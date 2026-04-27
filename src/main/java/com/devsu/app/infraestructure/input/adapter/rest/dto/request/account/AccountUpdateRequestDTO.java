package com.devsu.app.infraestructure.input.adapter.rest.dto.request.account;

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
public class AccountUpdateRequestDTO {

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Pattern(regexp = "^(Ahorro|Corriente)$", message = "El tipo debe ser Ahorro o Corriente")
    private String accountType;

    @NotNull(message = "El saldo es obligatorio")
    @DecimalMin(value = "0.00", message = "El saldo no puede ser negativo")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal balance;

    @NotNull(message = "El estado es obligatorio")
    private Short status;

    @DecimalMin(value = "0.00", message = "El límite diario no puede ser negativo")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal dailyLimit;
}
