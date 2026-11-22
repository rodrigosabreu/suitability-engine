package br.com.itau.investments.suitability.application.query;

import br.com.itau.investments.suitability.domain.model.assessment.SuitabilityAssessment;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssessmentQueryHandler {
    
    private final AssessmentRepository assessmentRepository;
    private final InvestorRepository investorRepository;
    
    public AssessmentQueryHandler(AssessmentRepository assessmentRepository, InvestorRepository investorRepository) {
        this.assessmentRepository = assessmentRepository;
        this.investorRepository = investorRepository;
    }
    
    public Optional<AssessmentDTO> getAssessment(AssessmentId id) {
        return assessmentRepository.findById(id)
            .map(this::toDTO);
    }
    
    public List<AssessmentDTO> getAssessmentsByInvestor(InvestorId investorId) {
        return assessmentRepository.findByInvestorId(id)
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    public List<AssessmentDTO> getAssessmentsByProduct(ProductId productId) {
        return assessmentRepository.findByProductId(id)
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    public List<AssessmentDTO> getPendingAssessments() {
        return assessmentRepository.findByStatus(AssessmentStatus.PENDENTE)
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    public List<AssessmentDTO> getPendingDocumentAssessments() {
        return assessmentRepository.findByStatus(AssessmentStatus.DOCUMENTO_PENDENTE)
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    public InvestorProfileDTO getInvestorProfile(InvestorId investorId) {
        return investorRepository.findById(investorId)
            .map(investor -> new InvestorProfileDTO(
                investor.getId(),
                investor.getProfile(),
                investor.getProfileAssessedAt(),
                investor.getProfileExpiresAt(),
                investor.isProfileValid(),
                investor.getKycStatus().name()
            ))
            .orElseThrow(() -> new IllegalArgumentException("Investidor não encontrado: " + investorId));
    }
    
    public record AssessmentDTO(
        AssessmentId id,
        InvestorId investorId,
        ProductId productId,
        InvestorProfile investorProfile,
        RiskLevel productRisk,
        AssessmentStatus status,
        boolean compatible,
        String violationCode,
        String documentHash,
        Instant createdAt,
        Instant updatedAt,
        Instant expiresAt
    ) {
        public static AssessmentDTO from(SuitabilityAssessment a) {
            return new AssessmentDTO(
                a.getId(),
                a.getInvestorId(),
                a.getProductId(),
                a.getInvestorProfile(),
                a.getProductRisk(),
                a.getStatus(),
                a.getCompatibilityResult() != null && a.getCompatibilityResult().compatible(),
                a.getCompatibilityResult() != null ? a.getCompatibilityResult().violationCode() : null,
                a.getDocumentHash(),
                a.getCreatedAt(),
                a.getUpdatedAt(),
                a.getExpiresAt()
            );
        }
    }
    
    public record InvestorProfileDTO(
        InvestorId id,
        InvestorProfile profile,
        Instant assessedAt,
        Instant expiresAt,
        boolean isValid,
        String kycStatus
    ) {}
}