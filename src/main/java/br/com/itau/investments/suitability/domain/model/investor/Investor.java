package br.com.itau.investments.suitability.domain.model.investor;

import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import java.time.Instant;

public class Investor {
    
    private final InvestorId id;
    private final String fullName;
    private final String cpfHash; // LGPD: hash do CPF
    private final String emailHash; // LGPD: hash do email
    private InvestorProfile profile;
    private KYCStatus kycStatus;
    private Instant profileAssessedAt;
    private Instant profileExpiresAt;
    private final Instant createdAt;
    private Instant updatedAt;
    
    public Investor(InvestorId id, String fullName, String cpfHash, String emailHash) {
        this.id = id;
        this.fullName = fullName;
        this.cpfHash = cpfHash;
        this.emailHash = emailHash;
        this.profile = InvestorProfile.CONSERVADOR; // Default conservador
        this.kycStatus = KYCStatus.PENDENTE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void updateProfile(InvestorProfile newProfile, Instant assessedAt) {
        this.profile = newProfile;
        this.profileAssessedAt = assessedAt;
        this.profileExpiresAt = assessedAt.plusSeconds(24 * 30 * 60 * 60); // 24 meses
        this.updatedAt = Instant.now();
    }
    
    public void updateKYCStatus(KYCStatus status) {
        this.kycStatus = status;
        this.updatedAt = Instant.now();
    }
    
    public boolean isProfileValid() {
        return this.profileExpiresAt != null && this.profileExpiresAt.isAfter(java.time.Instant.now());
    }
    
    public boolean isKYCComplete() {
        return this.kycStatus == KYCStatus.COMPLETO;
    }
    
    public int getProfileAgeMonths() {
        if (this.profileAssessedAt == null) return Integer.MAX_VALUE;
        return (int) java.time.Duration.between(this.profileAssessedAt, Instant.now()).toDays() / 30;
    }
    
    // Getters
    public InvestorId getId() { return id; }
    public String getFullName() { return fullName; }
    public String getCpfHash() { return cpfHash; }
    public String getEmailHash() { return emailHash; }
    public InvestorProfile getProfile() { return profile; }
    public KYCStatus getKycStatus() { return kycStatus; }
    public Instant getProfileAssessedAt() { return profileAssessedAt; }
    public Instant getProfileExpiresAt() { return profileExpiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    
    public enum KYCStatus {
        PENDENTE,
        EM_ANALISE,
        COMPLETO,
        REJEITADO,
        EXPIRADO
    }
}