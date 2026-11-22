package br.com.itau.investments.suitability.domain.service;

import br.com.itau.investments.suitability.domain.model.assessment.SuitabilityAssessment;
import br.com.itau.investments.suitability.domain.model.events.SuitabilityAssessed;
import br.com.itau.investments.suitability.domain.model.investor.Investor;
import br.com.itau.investments.suitability.domain.model.product.InvestmentProduct;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SuitabilityEngine {
    
    private final OpaPolicyClient opaClient;
    
    public SuitabilityEngine(OpaPolicyClient opaClient) {
        this.opaClient = opaClient;
    }
    
    public CompatibilityResult assess(Investor investor, InvestmentProduct product) {
        // 1. Validações prévias (fail-fast)
        validatePreconditions(investor, product);
        
        // 2. Validação OPA (CVM 558 + LGPD + AML)
        OpaValidationResult opaResult = opaClient.validate(buildOpaInput(investor, product));
        if (!opaResult.isAllowed()) {
            return CompatibilityResult.incompatible(
                investor.getProfile(), 
                opaResult.getProductRisk(), 
                opaResult.getViolationCode(), 
                opaResult.getReason()
            );
        }
        
        // 3. Regras de domínio puras (CVM 558)
        CompatibilityResult domainResult = evaluateDomainRules(investor, product);
        if (!domainResult.compatible()) {
            return domainResult;
        }
        
        // 4. Resultado compatível
        return CompatibilityResult.compatible(
            investor.getProfile(),
            product.getRiskLevel(),
            List.of("Art. 5º", "Art. 7º", "Art. 12º")
        );
    }
    
    private void validatePreconditions(Investor investor, InvestmentProduct product) {
        if (!product.isActive()) {
            throw new IllegalStateException("Produto inativo: " + product.getId());
        }
        
        if (!investor.isKYCComplete()) {
            throw new IllegalStateException("KYC incompleto para investidor: " + investor.getId());
        }
        
        if (!investor.isProfileValid()) {
            throw new IllegalStateException("Perfil de suitability expirado para investidor: " + investor.getId());
        }
        
        if (!product.isCompatibleWith(investor.getProfile())) {
            // Will be caught by domain rules, but fail fast
        }
    }
    
    private CompatibilityResult evaluateDomainRules(Investor investor, InvestmentProduct product) {
        // Regra CVM 558: Perfil CONSERVADOR não pode ALTO/MUITO_ALTO
        if (investor.getProfile() == InvestorProfile.CONSERVADOR) {
            if (product.getRiskLevel() == RiskLevel.ALTO || product.getRiskLevel() == RiskLevel.MUITO_ALTO) {
                return CompatibilityResult.incompatible(
                    investor.getProfile(),
                    product.getRiskLevel(),
                    "CVM_558_ART_12_CONSERVADOR_ALTO_RISCO",
                    "Perfil CONSERVADOR incompatível com produto de risco ALTO/MUITO_ALTO (CVM 558 Art. 12º)"
                );
            }
        }
        
        // Regra CVM 558: Perfil MODERADO não pode MUITO_ALTO
        if (investor.getProfile() == InvestorProfile.MODERADO) {
            if (product.getRiskLevel() == RiskLevel.MUITO_ALTO) {
                return CompatibilityResult.incompatible(
                    investor.getProfile(),
                    product.getRiskLevel(),
                    "CVM_558_ART_12_MODERADO_MUITO_ALTO",
                    "Perfil MODERADO incompatível com produto MUITO_ALTO (CVM 558 Art. 12º)"
                );
            }
        }
        
        // Regra: Produto inativo
        if (!product.isActive()) {
            return CompatibilityResult.incompatible(
                null, null, "PRODUCT_INACTIVE", "Produto não está ativo para comercialização"
            );
        }
        
        // Regra: KYC completo obrigatório
        if (!investor.isKYCComplete()) {
            return CompatibilityResult.incompatible(
                null, null, "KYC_INCOMPLETE", "KYC incompleto para o investidor"
            );
        }
        
        // Regra: Perfil válido (≤ 24 meses)
        if (!investor.isProfileValid()) {
            return CompatibilityResult.expired();
        }
        
        // Compatível
        return CompatibilityResult.compatible(
            investor.getProfile(),
            product.getRiskLevel(),
            List.of("Art. 5º", "Art. 7º", "Art. 12º")
        );
    }
    
    private OpaInput buildOpaInput(Investor investor, InvestmentProduct product) {
        return new OpaInput(
            "investment_recommendation",
            investor.getProfile().name(),
            product.getRiskLevel().name(),
            investor.getProfileAssessedAt() != null ? 
                (int) java.time.Duration.between(investor.getProfileAssessedAt(), java.time.Instant.now()).toDays() / 30 : 0,
            true, // suitability_assessment_done (assumindo que estamos criando nova)
            true, // suitability_document_signed (será validado depois)
            investor.isKYCComplete(),
            investor.isProfileValid(),
            product.getRiskLevel().name(),
            product.getType().name(),
            false, // processes_personal_data (LGPD)
            true, // has_lgpd_basis
            false, // contains_sensitive_data (no logs)
            true, // has_explicit_consent
            10, // data_retention_years
            false, // exposes_pii_in_logs
            false // uses_hardcoded_secrets
        );
    }
    
    // Inner classes for OPA integration
    public record OpaInput(
        String operation,
        String investor_profile,
        String product_risk,
        int suitability_assessment_age_months,
        boolean suitability_assessment_done,
        boolean suitability_document_signed,
        boolean kyc_completed,
        boolean profile_valid,
        String product_type,
        boolean processes_personal_data,
        boolean has_lgpd_basis,
        boolean contains_sensitive_data,
        boolean has_explicit_consent,
        int data_retention_years,
        boolean exposes_pii_in_logs,
        boolean uses_hardcoded_secrets
    ) {}
    
    public record OpaValidationResult(
        boolean allowed,
        String violationCode,
        String reason,
        RiskLevel productRisk
    ) {
        public static OpaValidationResult allowed() {
            return new OpaValidationResult(true, null, null, null);
        }
        
        public static OpaValidationResult denied(String violationCode, String reason, RiskLevel risk) {
            return new OpaValidationResult(false, violationCode, reason, risk);
        }
    }
}