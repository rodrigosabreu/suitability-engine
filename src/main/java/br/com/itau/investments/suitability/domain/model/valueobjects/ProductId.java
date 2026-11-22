package br.com.itau.investments.suitability.domain.model.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record ProductId(UUID value) {
    
    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }
    
    public static ProductId fromString(String value) {
        return new ProductId(UUID.fromString(value));
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}