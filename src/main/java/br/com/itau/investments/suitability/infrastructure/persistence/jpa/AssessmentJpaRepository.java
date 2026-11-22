package br.com.itau.investments.suitability.infrastructure.persistence.jpa;

import br.com.itau.investments.suitability.domain.model.assessment.SuitabilityAssessment;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentJpaRepository extends JpaRepository<SuitabilityAssessmentEntity, AssessmentId> {
    
    Optional<SuitabilityAssessmentEntity> findByIdempotencyKey(String idempotencyKey);
    
    List<SuitabilityAssessmentEntity> findByInvestorId(InvestorId investorId);
    
    List<SuitabilityAssessmentEntity> findByProductId(ProductId productId);
    
    List<SuitabilityAssessmentEntity> findByStatus(AssessmentStatus status);
    
    @Query("SELECT a FROM SuitabilityAssessmentEntity a WHERE a.investorId = :investorId AND a.status IN :statuses")
    List<SuitabilityAssessmentEntity> findByInvestorIdAndStatusIn(
        @Param("investorId") InvestorId investorId,
        @Param("statuses") List<AssessmentStatus> statuses
    );
}