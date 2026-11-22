package br.com.itau.investments.suitability.infrastructure.persistence.jpa;

import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "suitability_assessment")
@Getter
@Setter
public class SuitabilityAssessmentEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private AssessmentId id;
    
    @Column(name = "investor_id", columnDefinition = "uuid", nullable = false)
    private InvestorId investorId;
    
    @Column(name = "product_id", columnDefinition = "uuid", nullable = false)
    private ProductId productId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "investor_profile", nullable = false)
    private InvestorProfile investorProfile;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "product_risk", nullable = false)
    private RiskLevel productRisk;
    
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssessmentStatus status;
    
    @Column(name = "compatible")
    private Boolean compatible;
    
    @Column(name = "violation_code", length = 100)
    private String violationCode;
    
    @Column(name = "document_hash", length = 500)
    private String documentHash;
    
    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", columnDefinition = "jsonb")
    private Map<String, Object> conditions;
    
    @Column(name = "requires_document_signature")
    private Boolean requiresDocumentSignature;
    
    @Column(name = "document_hash")
    private String documentHash;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private int version = 0;
    
    // Constructors
    public SuitabilityAssessmentEntity() {}
    
    public SuitabilityAssessmentEntity(
            AssessmentId id, InvestorId investorId, ProductId productId,
            InvestorProfile profile, RiskLevel risk, String idempotencyKey) {
        this.id = id;
        this.investorId = investorId;
        this.productId = productId;
        this.investorProfile = profile;
        this.productRisk = risk;
        this.idempotencyKey = idempotencyKey;
        this.status = AssessmentStatus.PENDENTE;
        this.createdAt = java.time.Instant.now();
        this.updatedAt = java.time.Instant.now();
        this.expiresAt = java.time.Instant.now().plusSeconds(24 * 30 * 60 * 60);
    }
}