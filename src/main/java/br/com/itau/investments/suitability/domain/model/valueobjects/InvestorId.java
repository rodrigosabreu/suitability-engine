package br.com.itau.investments.suitability.domain.model.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record InvestorId(UUID value) {
    
    public static InvestorId generate() {
        return new InvestorId(UUID.randomUUID());
    }
    
    public static InvestorId fromString(String value) {
        return new InvestorId(UUID.fromString(value));
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}