package br.com.itau.investments.suitability.domain.model.valueobjects;

import java.util.List;

public record CompatibilityResult(
    boolean compatible,
    InvestorProfile investorProfile,
    RiskLevel productRisk,
    List<String> cvmArticles,
    String reasoning,
    List<String> conditions,
    boolean requiresDocumentSignature,
    String violationCode
) {
    
    public static CompatibilityResult compatible(InvestorProfile profile, RiskLevel risk, List<String> articles) {
        return new CompatibilityResult(
            true,
            profile,
            risk,
            articles,
            "Perfil compatível com o risco do produto conforme CVM 558",
            List.of(),
            true,
            null
        );
    }
    
    public static CompatibilityResult incompatible(InvestorProfile profile, RiskLevel risk, String violationCode, String reasoning) {
        return new CompatibilityResult(
            false,
            profile,
            risk,
            List.of("Art. 5º", "Art. 12º"),
            reasoning,
            List.of(),
            false,
            violationCode
        );
    }
    
    public static CompatibilityResult expired() {
        return new CompatibilityResult(
            false,
            null,
            null,
            List.of("Art. 5º", "Art. 12º"),
            "Avaliação de suitability expirada (> 24 meses)",
            List.of("Renovar avaliação de suitability"),
            false,
            "ASSESSMENT_EXPIRED"
        );
    }
    
    public static CompatibilityResult missingDocument() {
        return new CompatibilityResult(
            false,
            null,
            null,
            List.of("Art. 7º"),
            "Termo de suitability não assinado",
            List.of("Assinar termo de suitability"),
            true,
            "MISSING_SUITABILITY_DOCUMENT"
        );
    }
}