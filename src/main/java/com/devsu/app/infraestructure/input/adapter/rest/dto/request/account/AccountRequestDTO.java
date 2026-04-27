package com.devsu.app.infraestructure.input.adapter.rest.dto.request.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
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
public class AccountRequestDTO {

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Pattern(regexp = "^(Ahorro|Corriente)$", message = "El tipo debe ser Ahorro o Corriente")
    private String accountType;

    @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "El saldo debe tener máximo 2 decimales")
    private BigDecimal balance;

    @NotBlank(message = "El ID del cliente es obligatorio")
    private String clientId;
}
