package br.com.itau.investments.suitability.domain.model.product;

import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import java.math.BigDecimal;
import java.time.Instant;

public class InvestmentProduct {
    
    private final ProductId id;
    private final String name;
    private final String code; // Código interno/CVM
    private final ProductType type;
    private final RiskLevel riskLevel;
    private final BigDecimal minApplication;
    private final BigDecimal maxApplication;
    private final int liquidationDays; // D+0, D+1, D+2
    private final boolean hasPerformanceFee;
    private final BigDecimal managementFeeBps;
    private final BigDecimal performanceFeeBps;
    private final String benchmark; // CDI, Ibovespa, IMA-B, etc.
    private final boolean isActive;
    private final Instant createdAt;
    private Instant updatedAt;
    
    public InvestmentProduct(ProductId id, String name, String code, ProductType type,
                             RiskLevel riskLevel, BigDecimal minApplication,
                             int liquidationDays, String benchmark) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
        this.riskLevel = riskLevel;
        this.minApplication = minApplication;
        this.maxApplication = null; // Sem limite máximo padrão
        this.liquidationDays = liquidationDays;
        this.hasPerformanceFee = false;
        this.managementFeeBps = BigDecimal.ZERO;
        this.performanceFeeBps = BigDecimal.ZERO;
        this.benchmark = benchmark;
        this.isActive = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public boolean isCompatibleWith(InvestorProfile profile) {
        return this.riskLevel.isCompatibleWith(profile) && this.isActive;
    }
    
    public void updateRiskLevel(RiskLevel newRisk) {
        this.riskLevel = newRisk;
        this.updatedAt = Instant.now();
    }
    
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }
    
    public void activate() {
        this.isActive = true;
        this.updatedAt = Instant.now();
    }
    
    public void setFees(BigDecimal managementFeeBps, BigDecimal performanceFeeBps, boolean hasPerformanceFee) {
        this.managementFeeBps = managementFeeBps;
        this.performanceFeeBps = performanceFeeBps;
        this.hasPerformanceFee = hasPerformanceFee;
        this.updatedAt = Instant.now();
    }
    
    public boolean hasHurdleRate() {
        return this.hasPerformanceFee && this.benchmark != null && !this.benchmark.isBlank();
    }
    
    public int getRegulatoryMaxSettlementDays() {
        return switch (this.type) {
            case FUNDO_RF, FUNDO_MULTIMERCADO, FUNDO_ACOES -> 2; // D+2 fundos
            case ACAO, FUNDO_IMOBILIARIO -> 1; // D+1 ações
            case TITULO_PUBLICO -> 1; // D+1 títulos públicos
            case DEBENTURE, CRI, CRA -> 1; // D+1 renda fixa privada
            case DERIVATIVO -> 0; // D+0 derivativos
            case CAMBIO -> 1; // D+1 câmbio
            default -> 2;
        };
    }
    
    public boolean isFund() {
        return this.type == ProductType.FUNDO_RF 
            || this.type == ProductType.FUNDO_MULTIMERCADO
            || this.type == ProductType.FUNDO_ACOES
            || this.type == ProductType.FUNDO_IMOBILIARIO
            || this.type == ProductType.FIDC
            || this.type == ProductType.FIP;
    }
    
    public boolean requiresHurdleRate() {
        return this.hasPerformanceFee && this.benchmark != null;
    }
    
    // Getters
    public ProductId getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public ProductType getType() { return type; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public BigDecimal getMinApplication() { return minApplication; }
    public BigDecimal getMaxApplication() { return maxApplication; }
    public int getLiquidationDays() { return liquidationDays; }
    public boolean isHasPerformanceFee() { return hasPerformanceFee; }
    public BigDecimal getManagementFeeBps() { return managementFeeBps; }
    public BigDecimal getPerformanceFeeBps() { return performanceFeeBps; }
    public String getBenchmark() { return benchmark; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    
    public enum ProductType {
        FUNDO_RF,           // Fundo Renda Fixa
        FUNDO_MULTIMERCADO, // Fundo Multimercado
        FUNDO_ACOES,        // Fundo Ações
        FUNDO_IMOBILIARIO,  // Fundo Imobiliário (FII)
        FIDC,               // Fundo de Investimento em Direitos Creditórios
        FIP,                // Fundo de Investimento em Participações
        ETF,                // Exchange Traded Fund
        ACAO,               // Ação
        TITULO_PUBLICO,     // Título Público (Tesouro Direto)
        DEBENTURE,          // Debênture
        CRI,                // Certificado de Recebíveis Imobiliários
        CRA,                // Certificado de Recebíveis do Agronegócio
        DERIVATIVO,         // Derivativo (Opção, Futuro, Swap)
        CAMBIO,             // Câmbio / Moeda Estrangeira
        PREVIDENCIA_VGBL,   // Previdência VGBL
        PREVIDENCIA_PGBL,   // Previdência PGBL
        COE,                // Certificado de Operações Estruturadas
        STRUCTURED_NOTE     // Structured Note / Note Estruturada
    }
}