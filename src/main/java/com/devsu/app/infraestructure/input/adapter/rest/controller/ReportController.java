package com.devsu.app.infraestructure.input.adapter.rest.controller;

import com.devsu.app.domain.port.in.ReportUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.report.CustomerStatementResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.ReportMapper;
import com.devsu.app.infraestructure.output.report.PdfReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping(value = "/reportes", version = "1.0")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Generación de estados de cuenta")
public class ReportController {

    private final ReportUseCase reportUseCase;
    private final PdfReportService pdfReportService;
    private final ReportMapper reportMapper;

    @GetMapping
    @Operation(summary = "Obtener estado de cuenta en JSON")
    public Mono<ResponseEntity<CustomerStatementResponseDTO>> getStatement(
            @RequestParam String clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return reportUseCase.generateStatement(clientId, startDate, endDate)
                .map(reportMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/pdf")
    @Operation(summary = "Obtener estado de cuenta en PDF base64")
    public Mono<ResponseEntity<Map<String, String>>> getStatementPdf(
            @RequestParam String clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return reportUseCase.generateStatement(clientId, startDate, endDate)
                .flatMap(pdfReportService::generatePdf)
                .map(bytes -> {
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    return ResponseEntity.ok(Map.of(
                            "filename", "estado_cuenta_" + clientId + ".pdf",
                            "contentType", "application/pdf",
                            "data", base64
                    ));
                });
    }
}
