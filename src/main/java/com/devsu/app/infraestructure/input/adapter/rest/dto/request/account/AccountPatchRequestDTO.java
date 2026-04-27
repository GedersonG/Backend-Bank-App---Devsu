package com.devsu.app.infraestructure.input.adapter.rest.dto.request.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
public class AccountPatchRequestDTO {

    @Pattern(regexp = "^(Ahorro|Corriente)$", message = "El tipo debe ser Ahorro o Corriente")
    private String accountType;

    @DecimalMin(value = "0.00", message = "El saldo no puede ser negativo")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal balance;

    @DecimalMin(value = "0.00", message = "El límite diario no puede ser negativo")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal dailyLimit;

    private Short status;
}
