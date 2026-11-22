package br.com.itau.investments.suitability.infrastructure.persistence.jpa;

import br.com.itau.investments.suitability.domain.model.assessment.SuitabilityAssessment;
import br.com.itau.investments.suitability.domain.model.events.DomainEvent;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AssessmentMapper {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public SuitabilityAssessmentEntity toEntity(SuitabilityAssessment assessment) {
        SuitabilityAssessmentEntity entity;
        
        if (assessment.getId() != null) {
            // Existing entity - update
            entity = new SuitabilityAssessmentEntity();
            entity.setId(assessment.getId());
        } else {
            entity = new SuitabilityAssessmentEntity(
                assessment.getId(),
                assessment.getInvestorId(),
                assessment.getProductId(),
                assessment.getInvestorProfile(),
                assessment.getProductRisk(),
                assessment.getIdempotencyKey()
            );
        }
        
        entity.setInvestorId(assessment.getInvestorId());
        entity.setProductId(assessment.getProductId());
        entity.setInvestorProfile(assessment.getInvestorProfile());
        entity.setProductRisk(assessment.getProductRisk());
        entity.setIdempotencyKey(assessment.getIdempotencyKey());
        entity.setStatus(assessment.getStatus());
        entity.setCompatible(assessment.getCompatibilityResult() != null ? assessment.getCompatibilityResult().compatible() : null);
        entity.setViolationCode(assessment.getCompatibilityResult() != null ? assessment.getCompatibilityResult().violationCode() : null);
        entity.setDocumentHash(assessment.getDocumentHash());
        entity.setReasoning(assessment.getCompatibilityResult() != null ? assessment.getCompatibilityResult().reasoning() : null);
        entity.setConditions(assessment.getCompatibilityResult() != null ? 
            assessment.getCompatibilityResult().conditions().stream().collect(Collectors.toMap(
                k -> k, v -> v)) : Map.of());
        entity.setRequiresDocumentSignature(assessment.getCompatibilityResult() != null ? 
            assessment.getCompatibilityResult().requiresDocumentSignature() : false);
        entity.setUpdatedAt(assessment.getUpdatedAt());
        entity.setExpiresAt(assessment.getExpiresAt());
        entity.setVersion(assessment.getVersion());
        
        return entity;
    }
    
    public SuitabilityAssessment toDomain(SuitabilityAssessmentEntity entity) {
        var assessment = new SuitabilityAssessment();
        
        // Use reflection or manual mapping since SuitabilityAssessment has private constructor
        // For simplicity, we'll create a new instance and set fields via reflection
        // In production, you might want to use a proper mapping library or add package-private setters
        
        return rebuildAssessment(entity);
    }
    
    private SuitabilityAssessment rebuildAssessment(SuitabilityAssessmentEntity entity) {
        // This is a simplified reconstruction - in production you'd use a proper mapping approach
        // For now, returning a basic structure
        return null; // Placeholder - would need proper implementation
    }
}