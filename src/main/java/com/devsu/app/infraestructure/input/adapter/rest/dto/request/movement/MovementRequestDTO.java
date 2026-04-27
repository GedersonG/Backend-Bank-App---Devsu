package com.devsu.app.infraestructure.input.adapter.rest.dto.request.movement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MovementRequestDTO {

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String accountNumber;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Pattern(regexp = "^(Debito|Credito)$", message = "El tipo debe ser Debito o Credito")
    private String movementType;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor a cero")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal value;

    private String referenceAccountNumber;
}
