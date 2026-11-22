package br.com.itau.investments.suitability.infrastructure.security.opa;

import br.com.itau.investments.suitability.domain.service.SuitabilityEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OpaPolicyClient {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public OpaPolicyClient(
            @Value("${opa.url:http://localhost:8181}") String opaUrl,
            ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
            .baseUrl(opaUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.objectMapper = objectMapper;
    }
    
    public SuitabilityEngine.OpaValidationResult validate(SuitabilityEngine.OpaInput input) {
        try {
            String inputJson = objectMapper.writeValueAsString(input);
            
            Map<String, Object> request = Map.of(
                "input", objectMapper.readValue(inputJson, Map.class)
            );
            
            Map<String, Object> response = webClient.post()
                .uri("/v1/data/cvm558/suitability/deny")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            if (response != null && response.containsKey("result")) {
                @SuppressWarnings("unchecked")
                var result = (java.util.List<Map<String, Object>>) response.get("result");
                
                if (result != null && !result.isEmpty()) {
                    Map<String, Object> violation = result.get(0);
                    String msg = (String) violation.get("msg");
                    String violationCode = extractViolationCode(msg);
                    RiskLevel risk = extractRiskLevel(msg);
                    
                    return new SuitabilityEngine.OpaValidationResult(
                        false, violationCode, msg, extractRiskLevelFromInput());
                    }
            }
            
            return SuitabilityEngine.OpaValidationResult.allowed();
            
        } catch (Exception e) {
            // Fail-open para não bloquear em caso de falha do OPA
            // Log error e permite
            return SuitabilityEngine.OpaValidationResult.allowed();
        }
    }
    
    private String extractViolationCode(String message) {
        if (message.contains("CVM_558_ART_12_CONSERVADOR_ALTO_RISCO")) return "CVM_558_ART_12_CONSERVADOR_ALTO_RISCO";
        if (message.contains("CVM_558_ART_12_MODERADO_MUITO_ALTO")) return "CVM_558_ART_12_MODERADO_MUITO_ALTO";
        if (message.contains("CVM_558_ART_7")) return "CVM_558_ART_7_DOCUMENTO_NAO_ASSINADO";
        if (message.contains("CVM_558_ART_5")) return "CVM_558_ART_5_AVALIACAO_OBRIGATORIA";
        if (message.contains("CVM_175")) return "CVM_175_VIOLATION";
        if (message.contains("LGPD")) return "LGPD_VIOLATION";
        if (message.contains("AML")) return "AML_VIOLATION";
        if (message.contains("PCI-DSS")) return "PCI_DSS_VIOLATION";
        return "OPA_POLICY_VIOLATION";
    }
    
    private RiskLevel extractRiskLevel(String message) {
        if (message.contains("MUITO_ALTO")) return br.com.itau.investments.suitability.domain.model.valueobjects.RiskLevel.MUITO_ALTO;
        if (message.contains("ALTO")) return br.com.itau.investments.suitability.domain.model.valueobjects.RiskLevel.ALTO;
        if (message.contains("MEDIO")) return br.com.itau.investments.suitability.domain.model.valueobjects.RiskLevel.MEDIO;
        return br.com.itau.investments.suitability.domain.model.valueobjects.RiskLevel.BAIXO;
    }
    
    private RiskLevel extractRiskLevelFromInput() {
        // Seria extraído do input original
        return br.com.itau.investments.suitability.domain.model.valueobjects.RiskLevel.MEDIO;
    }
}