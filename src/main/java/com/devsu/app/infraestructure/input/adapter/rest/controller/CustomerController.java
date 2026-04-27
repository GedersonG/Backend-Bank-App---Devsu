package com.devsu.app.infraestructure.input.adapter.rest.controller;

import com.devsu.app.domain.port.in.CustomerUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.customer.CustomerResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.CustomerMapper;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes del banco")
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final CustomerMapper customerMapper;

    @GetMapping
    @Operation(summary = "Obtener clientes paginados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de clientes",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageResponseDTO.class)))
    })
    public Mono<ResponseEntity<PageResponseDTO<CustomerResponseDTO>>> findAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String identification,
            @RequestParam(required = false) String name
    ) {
        CustomerFilterDTO filter = CustomerFilterDTO.builder()
                .identification(identification)
                .name(name)
                .build();

        return customerUseCase.findAll(filter, page, pageSize)
                .map(customerMapper::toPageResponse)
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Obtener cliente por ID",
            description = "Retorna un cliente dado su identificador único de cliente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @GetMapping("/{clientId}")
    public Mono<ResponseEntity<CustomerResponseDTO>> findByCustomerId(
            @Parameter(description = "ID único del cliente", example = "CLI-001")
            @PathVariable String clientId
    ) {
        return customerUseCase.findByClientId(clientId)
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Crear cliente",
            description = "Registra un nuevo cliente junto con su información personal"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cliente ya existe", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<CustomerResponseDTO>> createCustomer(
            @RequestBody @Valid CustomerRequestDTO request
    ) {
        return customerUseCase.createCustomer(customerMapper.toDomain(request))
                .map(customerMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{clientId}")
    @Operation(summary = "Actualizar cliente completo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Identificación duplicada", content = @Content)
    })
    public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomer(
            @PathVariable String clientId,
            @RequestBody @Valid CustomerUpdateRequestDTO request
    ) {
        return customerUseCase.updateCustomer(clientId, customerMapper.updateToDomain(request))
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{clientId}")
    @Operation(summary = "Actualizar cliente parcialmente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    public Mono<ResponseEntity<CustomerResponseDTO>> patchCustomer(
            @PathVariable String clientId,
            @RequestBody CustomerPatchRequestDTO request
    ) {
        return customerUseCase.patchCustomer(clientId, request)
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{clientId}")
    @Operation(summary = "Eliminar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    public Mono<ResponseEntity<Void>> inactiveCustomer(@PathVariable String clientId) {
        return customerUseCase.deleteCustomer(clientId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
