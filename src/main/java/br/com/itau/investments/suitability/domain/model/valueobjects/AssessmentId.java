package br.com.itau.investments.suitability.domain.model.valueobjects;

import java.util.UUID;

public record AssessmentId(UUID value) {
    
    public static AssessmentId generate() {
        return new AssessmentId(java.util.UUID.randomUUID());
    }
    
    public static AssessmentId fromString(String value) {
        return new AssessmentId(java.util.UUID.fromString(value));
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}