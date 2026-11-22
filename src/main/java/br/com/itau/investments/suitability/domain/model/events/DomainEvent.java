package br.com.itau.investments.suitability.domain.model.events;

import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import java.time.Instant;
import java.util.Map;

public sealed interface DomainEvent permits 
    SuitabilityAssessmentCreated,
    SuitabilityAssessed,
    SuitabilityDocumentSigned,
    SuitabilityAssessmentApproved,
    SuitabilityAssessmentRejected,
    SuitabilityAssessmentExpired,
    SuitabilityAssessmentCancelled {
    
    String getEventType();
    AssessmentId getAggregateId();
    Instant getTimestamp();
    String getCorrelationId();
    String getCausationId();
    Map<String, Object> getMetadata();
    
    record SuitabilityAssessmentCreated(
        AssessmentId aggregateId,
        InvestorId investorId,
        ProductId productId,
        InvestorProfile investorProfile,
        RiskLevel productRisk,
        String idempotencyKey,
        Instant timestamp,
        String correlationId,
        String causationId,
        Map<String, Object> metadata
    ) implements DomainEvent {
        @Override public String getEventType() { return "SuitabilityAssessmentCreated"; }
        @Override public AssessmentId getAggregateId() { return aggregateId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public String getCausationId() { return causationId; }
        @Override public Map<String, Object> getMetadata() { return metadata; }
    }
    
    record SuitabilityAssessed(
        AssessmentId aggregateId,
        CompatibilityResult result,
        Instant timestamp,
        String correlationId,
        String causationId,
        Map<String, Object> metadata
    ) implements DomainEvent {
        @Override public String getEventType() { return "SuitabilityAssessed"; }
        @Override public AssessmentId getAggregateId() { return aggregateId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public String getCausationId() { return causationId; }
        @Override public Map<String, Object> getMetadata() { return metadata; }
    }
    
    record SuitabilityDocumentSigned(
        AssessmentId aggregateId,
        String documentHash,
        String signedBy,
        Instant timestamp,
        String correlationId,
        String causationId,
        Map<String, Object> metadata
    ) implements DomainEvent {
        @Override public String getEventType() { return "SuitabilityDocumentSigned"; }
        @Override public AssessmentId getAggregateId() { return aggregateId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public String getCausationId() { return causationId; }
        @Override public Map<String, Object> getMetadata() { return metadata; }
    }
    
    record SuitabilityAssessmentApproved(
        AssessmentId aggregateId,
        String approvedBy,
        Instant timestamp,
        String correlationId,
        String causationId,
        Map<String, Object> metadata
    ) implements DomainEvent {
        @Override public String getEventType() { return "SuitabilityAssessmentApproved"; }
        @Override public AssessmentId getAggregateId() { return aggregateId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public String getCausationId() { return causationId; }
        @Override public Map<String, Object> getMetadata() { return metadata; }
    }
    
    record SuitabilityAssessmentRejected(
        AssessmentId aggregateId,
        String reason,
        String rejectedBy,
        Instant timestamp,
        String correlationId,
        String causationId,
        Map<String, Object> metadata
    ) implements DomainEvent {
        @Override public String getEventType() { return "SuitabilityAssessmentRejected"; }
        @Override public AssessmentId getAggregateId() { return aggregateId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public String getCausationId() { return causationId; }
        @Override public Map<String, Object> getMetadata() { return metadata; }
    }
    
    record SuitabilityAssessmentExpired(
        AssessmentId aggregateId,
        Instant timestamp,
        String correlationId,
        String causationId,
        Map<String, Object> metadata
    ) implements DomainEvent {
        @Override public String getEventType() { return "SuitabilityAssessmentExpired"; }
        @Override public AssessmentId getAggregateId() { return aggregateId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public String getCausationId() { return causationId; }
        @Override public Map<String, Object> getMetadata() { return metadata; }
    }
    
    record SuitabilityAssessmentCancelled(
        AssessmentId aggregateId,
        String reason,
        String cancelledBy,
        Instant timestamp,
        String correlationId,
        String causationId,
        Map<String, Object> metadata
    ) implements DomainEvent {
        @Override public String getEventType() { return "SuitabilityAssessmentCancelled"; }
        @Override public AssessmentId getAggregateId() { return aggregateId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public String getCausationId() { return causationId; }
        @Override public Map<String, Object> getMetadata() { return metadata; }
    }
}