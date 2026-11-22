package br.com.itau.investments.suitability.infrastructure.observability.tracer;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class InvestmentsTracer {
    
    private static final String INVESTMENTS_PREFIX = "investments.";
    
    private static final AttributeKey<String> DOMAIN = AttributeKey.stringKey(INVESTMENTS_PREFIX + "domain");
    private static final AttributeKey<String> OPERATION_TYPE = AttributeKey.stringKey(INVESTMENTS_PREFIX + "operation_type");
    private static final AttributeKey<String> OPERATION = AttributeKey.stringKey(INVESTMENTS_PREFIX + "operation");
    private static final AttributeKey<String> ASSESSMENT_ID = AttributeKey.stringKey(INVESTMENTS_PREFIX + "assessment_id");
    private static final AttributeKey<String> INVESTOR_ID = AttributeKey.stringKey(INVESTMENTS_PREFIX + "investor_id");
    private static final AttributeKey<String> PRODUCT_ID = AttributeKey.stringKey(INVESTMENTS_PREFIX + "product_id");
    private static final AttributeKey<String> CORRELATION_ID = AttributeKey.stringKey(INVESTMENTS_PREFIX + "correlation_id");
    private static final AttributeKey<String> CAUSATION_ID = AttributeKey.stringKey(INVESTMENTS_PREFIX + "causation_id");
    private static final AttributeKey<Boolean> SUCCESS = AttributeKey.booleanKey(INVESTMENTS_PREFIX + "success");
    private static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey(INVESTMENTS_PREFIX + "error_type");
    private static final AttributeKey<Boolean> SLA_COMPLIANT = AttributeKey.booleanKey(INVESTMENTS_PREFIX + "sla_compliant");
    private static final AttributeKey<Long> LATENCY_MS = AttributeKey.longKey(INVESTMENTS_PREFIX + "latency_ms");
    private static final AttributeKey<Boolean> REGULATORY_CHECK = AttributeKey.booleanKey(INVESTMENTS_PREFIX + "regulatory_check");
    private static final AttributeKey<String> VIOLATION_CODE = AttributeKey.stringKey(INVESTMENTS_PREFIX + "violation_code");
    private static final AttributeKey<String> INVESTOR_PROFILE = AttributeKey.stringKey(INVESTMENTS_PREFIX + "investor_profile");
    private static final AttributeKey<String> PRODUCT_RISK = AttributeKey.stringKey(INVESTMENTS_PREFIX + "product_risk");
    private static final AttributeKey<String> VERSION = AttributeKey.stringKey(INVESTMENTS_PREFIX + "version");
    
    private final Tracer tracer;
    private final String serviceVersion;
    
    public InvestmentsTracer() {
        this.tracer = GlobalOpenTelemetry.getTracer("br.com.itau.investments.suitability", "1.0.0");
        this.serviceVersion = System.getProperty("app.version", "unknown");
    }
    
    public <T> T traceAssessment(String operation, String assessmentId, Supplier<T> fn) {
        return traceInternal("suitability", "assessment", operation, assessmentId, null, null, fn);
    }
    
    public <T> T traceAssessment(String operation, String assessmentId, String investorId, String productId, Supplier<T> fn) {
        return traceInternal("suitability", "assessment", operation, assessmentId, investorId, productId, fn);
    }
    
    public <T> T traceOpaCheck(String assessmentId, String policyName, Supplier<T> fn) {
        return traceInternal("suitability", "opa_check", policyName, null, null, null, 
            io.opentelemetry.api.common.Attributes.of(
                io.opentelemetry.api.common.AttributeKey.stringKey("investments.regulatory_check"), true
            ), fn);
    }
    
    public void traceSuitabilityViolation(String assessmentId, String violationCode, String investorProfile, String productRisk) {
        Span span = io.opentelemetry.api.GlobalOpenTelemetry.getTracer("br.com.itau.investments.suitability")
            .spanBuilder("suitability.violation")
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.domain"), "suitability")
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.operation_type"), "violation")
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.operation"), "violation_detected")
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.assessment_id"), assessmentId)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.violation_code"), violationCode)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.investor_profile"), assessmentId)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.product_risk"), assessmentId)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.booleanKey("investments.success"), false)
            .setSpanKind(SpanKind.INTERNAL)
            .startScopedSpan(span -> {
                span.recordException(new IllegalStateException("Suitability violation: " + assessmentId));
            });
    }
    
    public void traceLiquidityCheck(String assessmentId, String status, Duration latency, boolean slaCompliant) {
        Span span = io.opentelemetry.api.GlobalOpenTelemetry.getTracer("br.com.itau.investments.suitability")
            .spanBuilder("suitability.liquidity_check")
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.domain"), "suitability")
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.operation_type"), "liquidity_check")
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.assessment_id"), assessmentId)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.status"), assessmentId)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.longKey("investments.latency_ms"), latency.toMillis())
            .setAttribute(io.opentelemetry.api.common.AttributeKey.booleanKey("investments.sla_compliant"), slaCompliant)
            .setSpanKind(SpanKind.INTERNAL)
            .startScopedSpan(span -> {});
    }
    
    private <T> T traceInternal(String domain, String operationType, String operation, 
                                String assessmentId, String investorId, String productId, 
                                Supplier<T> fn) {
        return traceInternal(domain, operationType, operation, assessmentId, investorId, productId, null, fn);
    }
    
    private <T> T traceInternal(String domain, String operationType, String operation, 
                                String assessmentId, String investorId, String productId,
                                io.opentelemetry.api.common.Attributes extraAttributes, Supplier<T> fn) {
        
        String spanName = operationType + "." + domain + "." + operation;
        
        Span span = io.opentelemetry.api.GlobalOpenTelemetry.getTracer("br.com.itau.investments.suitability")
            .spanBuilder(spanName)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.domain"), domain)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.operation_type"), operationType)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.operation"), operation)
            .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.version"), "1.0.0")
            .setSpanKind(SpanKind.INTERNAL);
        
        if (assessmentId != null) span.setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.assessment_id"), assessmentId);
        if (investorId != null) span.setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.investor_id"), investorId);
        if (productId != null) span.setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.product_id"), productId);
        if (extraAttributes != null) span.setAllAttributes(extraAttributes);
        
        long startTime = System.currentTimeMillis();
        
        try (io.opentelemetry.context.Scope scope = span.makeCurrent()) {
            T result = fn.get();
            
            long latencyMs = System.currentTimeMillis() - startTime;
            span.setAttribute(io.opentelemetry.api.common.AttributeKey.booleanKey("investments.success"), true);
            span.setAttribute(io.opentelemetry.api.common.AttributeKey.longKey("investments.latency_ms"), latencyMs);
            
            return result;
        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - startTime;
            span.setAttribute(io.opentelemetry.api.common.AttributeKey.booleanKey("investments.success"), false);
            span.setAttribute(io.opentelemetry.api.common.AttributeKey.longKey("investments.latency_ms"), latencyMs);
            span.setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("investments.error_type"), e.getClass().getSimpleName());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}