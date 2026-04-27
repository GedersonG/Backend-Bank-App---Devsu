package com.devsu.app.infraestructure.input.adapter.rest.controller;

import com.devsu.app.domain.port.in.AccountUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.account.AccountResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.AccountMapper;
import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias")
public class AccountController {

    private final AccountUseCase accountUseCase;
    private final AccountMapper accountMapper;

    @GetMapping
    @Operation(summary = "Obtener cuentas")
    public Mono<ResponseEntity<PageResponseDTO<AccountResponseDTO>>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String customerIdentification
    ) {
        AccountFilterDTO filter = AccountFilterDTO.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .customerIdentification(customerIdentification)
                .build();

        return accountUseCase.findAll(filter, page, pageSize)
                .map(accountMapper::toPageResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    @Operation(summary = "Crear cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cuenta ya existe", content = @Content)
    })
    public Mono<ResponseEntity<AccountResponseDTO>> createAccount(
            @RequestBody @Valid AccountRequestDTO request
    ) {
        return accountUseCase.createAccount(accountMapper.toDomain(request))
                .map(accountMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Obtener cuenta por número")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public Mono<ResponseEntity<AccountResponseDTO>> findByAccountNumber(
            @Parameter(description = "Número de cuenta", example = "001-100001-00")
            @PathVariable String accountNumber
    ) {
        return accountUseCase.findByAccountNumber(accountNumber)
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Inactivar cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta inactivada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public Mono<ResponseEntity<Void>> deleteByAccountNumber(
            @Parameter(description = "Número de cuenta", example = "001-100001-00")
            @PathVariable String accountNumber
    ) {
        return accountUseCase.deleteByAccountNumber(accountNumber)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }

    @PutMapping("/{accountNumber}")
    @Operation(summary = "Actualizar cuenta completa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada",
                    content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public Mono<ResponseEntity<AccountResponseDTO>> update(
            @Parameter(description = "Número de cuenta", example = "001-100001-00")
            @PathVariable String accountNumber,
            @RequestBody @Valid AccountUpdateRequestDTO request
    ) {
        return accountUseCase.updateAccount(accountNumber, accountMapper.updateToDomain(request))
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{accountNumber}")
    @Operation(summary = "Actualizar cuenta parcialmente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada",
                    content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public Mono<ResponseEntity<AccountResponseDTO>> patch(
            @Parameter(description = "Número de cuenta", example = "001-100001-00")
            @PathVariable String accountNumber,
            @RequestBody AccountPatchRequestDTO request
    ) {
        return accountUseCase.patchAccount(accountNumber, request)
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok);
    }

}