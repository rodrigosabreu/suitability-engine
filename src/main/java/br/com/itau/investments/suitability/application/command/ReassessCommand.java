package br.com.itau.investments.suitability.application.command;

import br.com.itau.investments.suitability.domain.model.valueobjects.*;

public record ReassessCommand(
    AssessmentId assessmentId,
    String correlationId,
    String causationId
) {}