package br.com.itau.investments.suitability.application.command;

import br.com.itau.investments.suitability.domain.model.assessment.SuitabilityAssessment;
import br.com.itau.investments.suitability.domain.model.investor.Investor;
import br.com.itau.investments.suitability.domain.model.product.InvestmentProduct;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import br.com.itau.investments.suitability.domain.service.SuitabilityEngine;
import br.com.itau.investments.suitability.domain.model.events.*;
import br.com.itau.investments.suitability.infrastructure.persistence.eventstore.EventStoreRepository;
import br.com.itau.investments.suitability.infrastructure.messaging.kafka.KafkaEventPublisher;
import br.com.itau.investments.suitability.infrastructure.observability.tracer.InvestmentsTracer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AssessmentCommandHandler {
    
    private final InvestorRepository investorRepository;
    private final ProductRepository productRepository;
    private final AssessmentRepository assessmentRepository;
    private final SuitabilityEngine suitabilityEngine;
    private final EventStoreRepository eventStore;
    private final KafkaEventPublisher eventPublisher;
    private final InvestmentsTracer tracer;
    
    public AssessmentCommandHandler(
            InvestorRepository investorRepository,
            ProductRepository productRepository,
            AssessmentRepository assessmentRepository,
            SuitabilityEngine suitabilityEngine,
            EventStoreRepository eventStore,
            KafkaEventPublisher eventPublisher,
            InvestmentsTracer tracer) {
        this.investorRepository = investorRepository;
        this.productRepository = productRepository;
        this.assessmentRepository = assessmentRepository;
        this.suitabilityEngine = suitabilityEngine;
        this.eventStore = eventStore;
        this.eventPublisher = eventPublisher;
        this.tracer = tracer;
    }
    
    public AssessmentId handle(CreateAssessmentCommand command) {
        return tracer.traceCommand("suitability", "createAssessment", command.investorId().toString(), () -> {
            // 1. Carrega investidor e produto
            Investor investor = investorRepository.findById(command.investorId())
                .orElseThrow(() -> new IllegalArgumentException("Investidor não encontrado: " + command.investorId()));
            
            InvestmentProduct product = productRepository.findById(command.productId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + command.productId()));
            
            // 2. Verifica idempotência
            var existing = assessmentRepository.findByIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) {
                return existing.get().getId();
            }
            
            // 3. Cria assessment
            var assessment = SuitabilityAssessment.create(
                command.investorId(), command.productId(),
                investor.getProfile(), product.getRiskLevel(),
                command.idempotencyKey()
            );
            
            // 3. Avalia suitability
            var result = tracer.traceCommand("suitability", "assess", command.investorId().toString(), () -> {
                return suitabilityEngine.assess(investor, product);
            });
            
            // 4. Aplica resultado
            assessment.assess(result);
            
            // 5. Persiste
            assessmentRepository.save(assessment);
            eventStore.appendAll(assessment.getUncommittedEvents());
            assessment.clearEvents();
            
            // 6. Publica eventos
            eventPublisher.publishAll(assessment.getUncommittedEvents());
            
            return assessment.getId();
        });
    }
    
    public void handle(SignDocumentCommand command) {
        tracer.traceCommand("suitability", "signDocument", command.assessmentId().toString(), () -> {
            var assessment = assessmentRepository.findById(command.assessmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assessment não encontrado: " + command.assessmentId()));
            
            assessment.signDocument(command.documentHash(), command.signedBy());
            
            assessmentRepository.save(assessment);
            eventStore.appendAll(assessment.getUncommittedEvents());
            assessment.clearEvents();
            
            eventPublisher.publishAll(assessment.getUncommittedEvents());
        });
    }
    
    public void handle(ReassessCommand command) {
        tracer.traceCommand("suitability", "reassess", command.assessmentId().toString(), () -> {
            var assessment = assessmentRepository.findById(command.assessmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assessment não encontrado: " + command.assessmentId()));
            
            // Recarrega investidor e produto
            Investor investor = investorRepository.findById(assessment.getInvestorId())
                .orElseThrow(() -> new IllegalArgumentException("Investidor não encontrado"));
            InvestmentProduct product = productRepository.findById(assessment.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
            
            // Reavalia
            var investorRef = investor; // Para capturar no lambda
            var productRef = product;
            var result = tracer.traceCommand("suitability", "reassess", assessment.getId().toString(), () -> {
                return suitabilityEngine.assess(investorRef, productRef);
            });
            
            assessment.assess(result);
            
            assessmentRepository.save(assessment);
            eventStore.appendAll(assessment.getUncommittedEvents());
            assessment.clearEvents();
            
            eventPublisher.publishAll(assessment.getUncommittedEvents());
        });
    }
    
    public void handle(ExpireAssessmentCommand command) {
        tracer.traceCommand("suitability", "expire", command.assessmentId().toString(), () -> {
            var assessment = assessmentRepository.findById(command.assessmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assessment não encontrado: " + command.assessmentId()));
            
            assessment.expire();
            
            assessmentRepository.save(assessment);
            eventStore.appendAll(assessment.getUncommittedEvents());
            assessment.clearEvents();
            
            eventPublisher.publishAll(assessment.getUncommittedEvents());
        });
    }
}