package br.com.itau.investments.suitability.domain.model.valueobjects;

public enum AssessmentStatus {
    PENDENTE,           // Aguardando avaliação
    EM_ANALISE,         // Em processamento
    APROVADO,           // Compatível - termo assinado
    REJEITADO,          // Incompatível (CVM 558)
    EXPIRADO,           // Validade expirada (> 24 meses)
    CANCELADO,          // Cancelado pelo investidor/gestor
    DOCUMENTO_PENDENTE; // Aguardando assinatura do termo
    
    public boolean isFinal() {
        return this == APROVADO || this == REJEITADO || this == EXPIRADO || this == CANCELADO;
    }
    
    public boolean isActive() {
        return this == PENDENTE || this == EM_ANALISE || this == DOCUMENTO_PENDENTE;
    }
    
    public boolean requiresDocumentSignature() {
        return this == DOCUMENTO_PENDENTE;
    }
}