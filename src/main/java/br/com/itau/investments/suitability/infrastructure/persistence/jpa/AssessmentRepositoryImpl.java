package br.com.itau.investments.suitability.infrastructure.persistence.jpa;

import br.com.itau.investments.suitability.domain.model.assessment.SuitabilityAssessment;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AssessmentRepositoryImpl implements AssessmentRepository {
    
    private final AssessmentJpaRepository jpaRepository;
    private final AssessmentMapper mapper;
    
    public AssessmentRepositoryImpl(AssessmentJpaRepository jpaRepository, AssessmentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public void save(SuitabilityAssessment assessment) {
        var entity = mapper.toEntity(assessment);
        jpaRepository.save(entity);
    }
    
    @Override
    public Optional<SuitabilityAssessment> findById(AssessmentId id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Optional<SuitabilityAssessment> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }
    
    @Override
    public List<SuitabilityAssessment> findByInvestorId(InvestorId investorId) {
        return jpaRepository.findByInvestorId(investorId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<SuitabilityAssessment> findByProductId(ProductId productId) {
        return jpaRepository.findByProductId(productId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<SuitabilityAssessment> findByStatus(AssessmentStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}