package br.com.itau.investments.suitability.infrastructure.persistence.eventstore;

import br.com.itau.investments.suitability.domain.model.events.DomainEvent;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventStoreRepository {
    
    void append(DomainEvent event);
    
    void appendAll(List<DomainEvent> events);
    
    List<DomainEvent> findByAggregateId(String aggregateId);
    
    List<DomainEvent> findByCorrelationId(String correlationId);
    
    Optional<DomainEvent> findById(String eventId);
    
    VerificationResult verifyChain(String aggregateId);
    
    record VerificationResult(
        boolean success,
        String eventId,
        String message
    ) {
        public static VerificationResult success() {
            return new VerificationResult(true, null, null);
        }
        
        public static VerificationResult failed(String eventId, String message) {
            return new VerificationResult(false, eventId, message);
        }
    }
}