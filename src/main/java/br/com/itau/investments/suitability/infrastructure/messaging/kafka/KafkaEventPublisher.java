package br.com.itau.investments.suitability.infrastructure.messaging.kafka;

import br.com.itau.investments.suitability.domain.model.events.DomainEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topic.suitability.assessed:itau.investments.suitability.assessed}")
    private String assessedTopic;
    
    @Value("${kafka.topic.suitability.document_signed:itau.investments.suitability.document_signed}")
    private String documentSignedTopic;
    
    @Value("${kafka.topic.suitability.approved:itau.investments.suitability.approved}")
    private String approvedTopic;
    
    @Value("${kafka.topic.suitability.rejected:itau.investments.suitability.rejected}")
    private String rejectedTopic;
    
    @Value("${kafka.topic.suitability.expired:itau.investments.suitability.expired}")
    private String expiredTopic;
    
    @Value("${kafka.topic.suitability.cancelled:itau.investments.suitability.cancelled}")
    private String cancelledTopic;
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishAll(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            publish(event);
        }
    }
    
    public void publish(DomainEvent event) {
        String topic = resolveTopic(event.getEventType());
        String key = event.getAggregateId().value();
        
        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // Log error, alert monitoring
                System.err.println("Failed to publish event: " + event.getEventType() + " - " + ex.getMessage());
            }
        });
    }
    
    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "SuitabilityAssessed" -> assessedTopic;
            case "SuitabilityDocumentSigned" -> documentSignedTopic;
            case "SuitabilityAssessmentApproved" -> approvedTopic;
            case "SuitabilityAssessmentRejected" -> rejectedTopic;
            case "SuitabilityAssessmentExpired" -> expiredTopic;
            case "SuitabilityAssessmentCancelled" -> cancelledTopic;
            default -> "itau.investments.suitability.events";
        };
    }
}