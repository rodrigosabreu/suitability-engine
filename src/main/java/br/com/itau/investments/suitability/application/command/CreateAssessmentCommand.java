package br.com.itau.investments.suitability.application.command;

import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import java.time.Instant;

public record CreateAssessmentCommand(
    InvestorId investorId,
    ProductId productId,
    String idempotencyKey,
    String correlationId,
    String causationId
) {
    public static CreateAssessmentCommand of(InvestorId investorId, ProductId productId) {
        return new CreateAssessmentCommand(
            investorId, productId, 
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            null
        );
    }
}