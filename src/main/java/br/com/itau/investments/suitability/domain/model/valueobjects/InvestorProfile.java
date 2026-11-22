package br.com.itau.investments.suitability.domain.model.valueobjects;

public enum InvestorProfile {
    CONSERVADOR,
    MODERADO,
    ARROJADO,
    QUALIFICADO,
    PROFISSIONAL;
    
    public boolean canInvestIn(RiskLevel productRisk) {
        return productRisk.isCompatibleWith(this);
    }
    
    public int getNumericValue() {
        return switch (this) {
            case CONSERVADOR -> 1;
            case MODERADO -> 2;
            case ARROJADO -> 3;
            case QUALIFICADO -> 4;
            case PROFISSIONAL -> 5;
        };
    }
    
    public static InvestorProfile fromNumeric(int value) {
        return switch (value) {
            case 1 -> CONSERVADOR;
            case 2 -> MODERADO;
            case 3 -> ARROJADO;
            case 4 -> QUALIFICADO;
            case 5 -> PROFISSIONAL;
            default -> throw new IllegalArgumentException("Invalid investor profile: " + value);
        };
    }
    
    public boolean isQualifiedOrProfessional() {
        return this == QUALIFICADO || this == PROFISSIONAL;
    }
}