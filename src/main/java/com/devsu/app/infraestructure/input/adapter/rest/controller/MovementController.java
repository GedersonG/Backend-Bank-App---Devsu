package com.devsu.app.infraestructure.input.adapter.rest.controller;

import com.devsu.app.domain.port.in.MovementUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.movement.MovementRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.movement.MovementResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.MovementMapper;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos", description = "Gestión de movimientos bancarios")
public class MovementController {

    private final MovementUseCase movementUseCase;
    private final MovementMapper movementMapper;

    @PostMapping
    @Operation(summary = "Crear movimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimiento creado",
                    content = @Content(schema = @Schema(implementation = MovementResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente o cuenta inactiva", content = @Content)
    })
    public Mono<ResponseEntity<MovementResponseDTO>> create(
            @RequestBody @Valid MovementRequestDTO request
    ) {
        return movementUseCase.createMovement(movementMapper.toDomain(request))
                .map(movementMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping
    @Operation(summary = "Obtener movimientos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de movimientos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageResponseDTO.class)))
    })
    public Mono<ResponseEntity<PageResponseDTO<MovementResponseDTO>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "Número de cuenta")
            @RequestParam(required = false) String accountNumber,
            @Parameter(description = "Fecha inicial (ISO 8601)", example = "2026-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha final (ISO 8601)", example = "2026-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        MovementFilterDTO filter = MovementFilterDTO.builder()
                .accountNumber(accountNumber)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return movementUseCase.findAll(filter, page, pageSize)
                .map(movementMapper::toPageResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{movementId}/reversar")
    @Operation(summary = "Reversar un movimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento reversado",
                    content = @Content(schema = @Schema(implementation = MovementResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Movimiento ya reversado", content = @Content)
    })
    public Mono<ResponseEntity<MovementResponseDTO>> reverse(
            @Parameter(description = "ID del movimiento a reversar")
            @PathVariable Long movementId
    ) {
        return movementUseCase.reverseMovement(movementId)
                .map(movementMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
