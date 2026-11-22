package br.com.itau.investments.suitability.domain.model.valueobjects;

public enum RiskLevel {
    BAIXO,
    MEDIO,
    ALTO,
    MUITO_ALTO;
    
    public boolean isCompatibleWith(InvestorProfile profile) {
        return switch (this) {
            case BAIXO, MEDIO -> true; // Compatível com todos os perfis
            case ALTO -> profile != InvestorProfile.CONSERVADOR;
            case MUITO_ALTO -> profile == InvestorProfile.ARROJADO 
                            || profile == InvestorProfile.QUALIFICADO 
                            || profile == InvestorProfile.PROFISSIONAL;
        };
    }
    
    public int getNumericValue() {
        return switch (this) {
            case BAIXO -> 1;
            case MEDIO -> 2;
            case ALTO -> 3;
            case MUITO_ALTO -> 4;
        };
    }
    
    public static RiskLevel fromNumeric(int value) {
        return switch (value) {
            case 1 -> BAIXO;
            case 2 -> MEDIO;
            case 3 -> ALTO;
            case 4 -> MUITO_ALTO;
            default -> throw new IllegalArgumentException("Invalid risk level: " + value);
        };
    }
}