package br.com.itau.investments.suitability.application.command;

import br.com.itau.investments.suitability.domain.model.valueobjects.*;

public record SignDocumentCommand(
    AssessmentId assessmentId,
    String documentHash,
    String signedBy,
    String correlationId,
    String causationId
) {}