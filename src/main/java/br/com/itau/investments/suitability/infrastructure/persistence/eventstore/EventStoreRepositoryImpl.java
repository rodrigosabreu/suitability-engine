package br.com.itau.investments.suitability.infrastructure.persistence.eventstore;

import br.com.itau.investments.suitability.domain.model.events.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EventStoreRepositoryImpl implements EventStoreRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    public EventStoreRepositoryImpl(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void append(DomainEvent event) {
        String sql = """
            INSERT INTO event_store (event_id, aggregate_id, event_type, payload, 
                                     correlation_id, causation_id, timestamp, hash_chain)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
        
        String hashChain = calculateHashChain(event.getAggregateId().value(), payload);
        
        jdbcTemplate.update(sql,
            event.getEventId(),
            event.getAggregateId().value(),
            event.getEventType(),
            payload,
            event.getCorrelationId(),
            event.getCausationId(),
            event.getTimestamp(),
            hashChain
        );
    }
    
    @Override
    public void appendAll(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            append(event);
        }
    }
    
    @Override
    public List<DomainEvent> findByAggregateId(String aggregateId) {
        String sql = "SELECT * FROM event_store WHERE aggregate_id = ? ORDER BY timestamp";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                String payload = rs.getString("payload");
                return objectMapper.readValue(payload, DomainEvent.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize event", e);
            }
        }, aggregateId);
    }
    
    @Override
    public List<DomainEvent> findByCorrelationId(String correlationId) {
        String sql = "SELECT * FROM event_store WHERE correlation_id = ? ORDER BY timestamp";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                String payload = rs.getString("payload");
                return objectMapper.readValue(payload, DomainEvent.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize event", e);
            }
        }, correlationId);
    }
    
    @Override
    public Optional<DomainEvent> findById(String eventId) {
        String sql = "SELECT * FROM event_store WHERE event_id = ?";
        
        List<DomainEvent> events = jdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                String payload = rs.getString("payload");
                return objectMapper.readValue(payload, DomainEvent.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize event", e);
            }
        }, eventId);
        
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(0));
    }
    
    @Override
    public VerificationResult verifyChain(String aggregateId) {
        List<DomainEvent> events = findByAggregateId(aggregateId);
        
        String expectedHash = null;
        for (DomainEvent event : events) {
            String payload;
            try {
                payload = objectMapper.writeValueAsString(event);
            } catch (Exception e) {
                return VerificationResult.failed(event.getEventId(), "Serialization failed: " + e.getMessage());
            }
            
            String calculated = calculateHashChain(expectedHash, payload, event.getTimestamp());
            
            // Get stored hash from DB
            String storedHash = jdbcTemplate.queryForObject(
                "SELECT hash_chain FROM event_store WHERE event_id = ?", 
                String.class, event.getEventId()
            );
            
            if (!calculated.equals(storedHash)) {
                return VerificationResult.failed(event.getEventId(), "Hash chain broken");
            }
            
            expectedHash = calculated;
        }
        
        return VerificationResult.success();
    }
    
    private String calculateHashChain(String previousHash, String payload, java.time.Instant timestamp) {
        String input = (previousHash != null ? previousHash : "") + payload + timestamp.toEpochMilli();
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
    
    private String calculateHashChain(String previousHash, String payload) {
        return calculateHashChain(previousHash, payload, java.time.Instant.now());
    }
}