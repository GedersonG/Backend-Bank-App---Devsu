package com.devsu.app.infraestructure.input.adapter.rest.dto.response.account;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class AccountResponseDTO {
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private Short status;
    private String clientId;
    private String customerName;
    private String customerIdentification;
    private LocalDateTime createdAt;
}
