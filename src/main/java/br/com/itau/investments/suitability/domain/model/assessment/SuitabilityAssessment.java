package br.com.itau.investments.suitability.domain.model.assessment;

import br.com.itau.investments.suitability.domain.model.events.*;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SuitabilityAssessment {
    
    private final AssessmentId id;
    private final InvestorId investorId;
    private final ProductId productId;
    private final InvestorProfile investorProfile;
    private final RiskLevel productRisk;
    private final String idempotencyKey;
    private AssessmentStatus status;
    private CompatibilityResult compatibilityResult;
    private String documentHash;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    private int version = 0;
    
    private SuitabilityAssessment() {
        this.id = AssessmentId.generate();
        this.idempotencyKey = UUID.randomUUID().toString();
        this.status = AssessmentStatus.PENDENTE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(24 * 30 * 60 * 60); // 24 meses
    }
    
    public static SuitabilityAssessment create(InvestorId investorId, ProductId productId, 
                                               InvestorProfile profile, RiskLevel risk, 
                                               String idempotencyKey) {
        SuitabilityAssessment assessment = new SuitabilityAssessment();
        assessment.investorId = investorId;
        assessment.productId = productId;
        assessment.investorProfile = profile;
        assessment.productRisk = risk;
        assessment.idempotencyKey = idempotencyKey;
        assessment.expiresAt = Instant.now().plusSeconds(24 * 30 * 60 * 60); // 24 meses
        
        assessment.registerEvent(new SuitabilityAssessmentCreated(
            assessment.id, investorId, productId, profile, risk, 
            idempotencyKey, Instant.now(), UUID.randomUUID().toString(), null, Map.of()
        ));
        
        return assessment;
    }
    
    public void assess(CompatibilityResult result) {
        if (this.status != AssessmentStatus.PENDENTE && this.status != AssessmentStatus.EM_ANALISE) {
            throw new IllegalStateException("Cannot assess assessment with status: " + this.status);
        }
        
        this.status = AssessmentStatus.EM_ANALISE;
        this.compatibilityResult = result;
        this.updatedAt = Instant.now();
        
        if (result.compatible()) {
            this.status = AssessmentStatus.DOCUMENTO_PENDENTE;
            registerEvent(new SuitabilityAssessed(this.id, result, Instant.now(), 
                UUID.randomUUID().toString(), null, Map.of()));
            registerEvent(new SuitabilityAssessmentCreated(
                this.id, this.investorId, this.productId, this.investorProfile, 
                this.productRisk, this.idempotencyKey, Instant.now(), 
                UUID.randomUUID().toString(), null, Map.of()));
        } else {
            this.status = AssessmentStatus.REJEITADO;
            registerEvent(new SuitabilityAssessed(this.id, result, Instant.now(), 
                UUID.randomUUID().toString(), null, Map.of()));
            registerEvent(new SuitabilityAssessmentRejected(
                this.id, result.reasoning(), "system", Instant.now(), 
                UUID.randomUUID().toString(), null, Map.of("violationCode", result.violationCode())));
        }
    }
    
    public void signDocument(String documentHash, String signedBy) {
        if (this.status != AssessmentStatus.DOCUMENTO_PENDENTE) {
            throw new IllegalStateException("Cannot sign document for status: " + this.status);
        }
        
        this.documentHash = documentHash;
        this.status = AssessmentStatus.APROVADO;
        this.updatedAt = Instant.now();
        
        registerEvent(new SuitabilityDocumentSigned(
            this.id, documentHash, signedBy, Instant.now(), 
            UUID.randomUUID().toString(), null, Map.of()));
        
        registerEvent(new SuitabilityAssessmentApproved(
            this.id, "system", Instant.now(), UUID.randomUUID().toString(), null, Map.of()));
    }
    
    public void expire() {
        if (this.status.isFinal()) {
            return; // Already final, idempotent
        }
        this.status = AssessmentStatus.EXPIRADO;
        this.updatedAt = Instant.now();
        registerEvent(new SuitabilityAssessmentExpired(
            this.id, Instant.now(), UUID.randomUUID().toString(), null, Map.of()));
    }
    
    public void cancel(String reason, String cancelledBy) {
        if (this.status.isFinal()) {
            return; // Already final, idempotent
        }
        this.status = AssessmentStatus.CANCELADO;
        this.updatedAt = Instant.now();
        registerEvent(new SuitabilityAssessmentCancelled(
            this.id, reason, cancelledBy, Instant.now(), UUID.randomUUID().toString(), null, Map.of()));
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }
    
    public void checkExpiration() {
        if (isExpired() && this.status.isActive()) {
            expire();
        }
    }
    
    public void registerEvent(DomainEvent event) {
        this.uncommittedEvents.add(event);
        this.version++;
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return List.copyOf(uncommittedEvents);
    }
    
    public void clearEvents() {
        this.uncommittedEvents.clear();
    }
    
    // Getters
    public AssessmentId getId() { return id; }
    public InvestorId getInvestorId() { return investorId; }
    public ProductId getProductId() { return productId; }
    public InvestorProfile getInvestorProfile() { return investorProfile; }
    public RiskLevel getProductRisk() { return productRisk; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public AssessmentStatus getStatus() { return status; }
    public CompatibilityResult getCompatibilityResult() { return compatibilityResult; }
    public String getDocumentHash() { return documentHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public int getVersion() { return version; }
}