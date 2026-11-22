package br.com.itau.investments.suitability.infrastructure.persistence.jpa;

import br.com.itau.investments.suitability.domain.model.assessment.SuitabilityAssessment;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository {
    
    void save(SuitabilityAssessment assessment);
    
    Optional<SuitabilityAssessment> findById(AssessmentId id);
    
    Optional<SuitabilityAssessment> findByIdempotencyKey(String idempotencyKey);
    
    List<SuitabilityAssessment> findByInvestorId(InvestorId investorId);
    
    List<SuitabilityAssessment> findByProductId(ProductId productId);
    
    List<SuitabilityAssessment> findByStatus(AssessmentStatus status);
}