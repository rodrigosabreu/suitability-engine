package br.com.itau.investments.suitability.interfaces.rest;

import br.com.itau.investments.suitability.application.command.*;
import br.com.itau.investments.suitability.application.query.AssessmentQueryHandler;
import br.com.itau.investments.suitability.domain.model.valueobjects.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments")
@Tag(name = "Suitability Assessment", description = "CVM 558 Suitability Assessment API")
public class AssessmentController {
    
    private final AssessmentCommandHandler commandHandler;
    private final AssessmentQueryHandler queryHandler;
    
    public AssessmentController(AssessmentCommandHandler commandHandler, AssessmentQueryHandler queryHandler) {
        this.commandHandler = commandHandler;
        this.queryHandler = queryHandler;
    }
    
    @PostMapping
    @Operation(summary = "Criar nova avaliação de suitability")
    public ResponseEntity<AssessmentResponse> createAssessment(@Valid @RequestBody CreateAssessmentRequest request) {
        var command = new CreateAssessmentCommand(
            new InvestorId(UUID.fromString(request.investorId())),
            new ProductId(UUID.fromString(request.productId())),
            request.idempotencyKey(),
            UUID.randomUUID().toString(),
            null
        );
        
        AssessmentId assessmentId = commandHandler.handle(new CreateAssessmentCommand(
            new InvestorId(UUID.fromString(request.investorId())),
            new ProductId(UUID.fromString(request.productId())),
            request.idempotencyKey(),
            UUID.randomUUID().toString(),
            null
        ));
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AssessmentResponse(assessmentId.toString(), "CREATED"));
    }
    
    @PostMapping("/{assessmentId}/sign")
    @Operation(summary = "Assinar termo de suitability")
    public ResponseEntity<AssessmentResponse> signDocument(
            @PathVariable String assessmentId,
            @Valid @RequestBody SignDocumentRequest request) {
        
        commandHandler.handle(new SignDocumentCommand(
            new AssessmentId(UUID.fromString(assessmentId)),
            request.documentHash(),
            request.signedBy(),
            UUID.randomUUID().toString(),
            null
        ));
        
        return ResponseEntity.ok(new AssessmentResponse(assessmentId, "SIGNED"));
    }
    
    @PostMapping("/{assessmentId}/reassess")
    @Operation(summary = "Reavaliar suitability")
    public ResponseEntity<AssessmentResponse> reassess(@PathVariable String assessmentId) {
        commandHandler.handle(new ReassessCommand(
            new AssessmentId(UUID.fromString(assessmentId)),
            UUID.randomUUID().toString(),
            null
        ));
        
        return ResponseEntity.ok(new AssessmentResponse(assessmentId, "REASSESSED"));
    }
    
    @PostMapping("/{assessmentId}/expire")
    @Operation(summary = "Expirar avaliação")
    public ResponseEntity<AssessmentResponse> expire(@PathVariable String assessmentId) {
        commandHandler.handle(new ExpireAssessmentCommand(
            new AssessmentId(UUID.fromString(assessmentId)),
            UUID.randomUUID().toString(),
            null
        ));
        
        return ResponseEntity.ok(new AssessmentResponse(assessmentId, "EXPIRED"));
    }
    
    @GetMapping("/{assessmentId}")
    @Operation(summary = "Buscar assessment por ID")
    public ResponseEntity<AssessmentQueryHandler.AssessmentDTO> getAssessment(@PathVariable String assessmentId) {
        return queryHandler.getAssessment(new AssessmentId(UUID.fromString(assessmentId)))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/investor/{investorId}")
    @Operation(summary = "Listar assessments por investidor")
    public ResponseEntity<List<AssessmentQueryHandler.AssessmentDTO>> getByInvestor(@PathVariable String investorId) {
        var assessments = queryHandler.getAssessmentsByInvestor(new InvestorId(UUID.fromString(investorId)));
        return ResponseEntity.ok(assessments);
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Listar assessments por produto")
    public ResponseEntity<List<AssessmentQueryHandler.AssessmentDTO>> getByProduct(@PathVariable String productId) {
        var assessments = queryHandler.getAssessmentsByProduct(new ProductId(UUID.fromString(productId)));
        return ResponseEntity.ok(assessments);
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Listar assessments pendentes")
    public ResponseEntity<List<AssessmentQueryHandler.AssessmentDTO>> getPending() {
        return ResponseEntity.ok(queryHandler.getPendingAssessments());
    }
    
    @GetMapping("/pending-document")
    @Operation(summary = "Listar assessments aguardando documento")
    public ResponseEntity<List<AssessmentQueryHandler.AssessmentDTO>> getPendingDocument() {
        return ResponseEntity.ok(queryHandler.getPendingDocumentAssessments());
    }
    
    @GetMapping("/investor/{investorId}/profile")
    @Operation(summary = "Buscar perfil do investidor")
    public ResponseEntity<AssessmentQueryHandler.InvestorProfileDTO> getInvestorProfile(@PathVariable String investorId) {
        return ResponseEntity.ok(queryHandler.getInvestorProfile(new InvestorId(UUID.fromString(investorId))));
    }
    
    // Request/Response DTOs
    public record CreateAssessmentRequest(
        String investorId,
        String productId,
        String idempotencyKey
    ) {}
    
    public record SignDocumentRequest(
        String documentHash,
        String signedBy
    ) {}
    
    public record AssessmentResponse(
        String assessmentId,
        String status
    ) {}
}