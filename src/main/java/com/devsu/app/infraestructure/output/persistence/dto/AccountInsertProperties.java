package com.devsu.app.infraestructure.output.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AccountInsertProperties {
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private Short status;
    private Long customerId;
    private String createdBy;
    private LocalDateTime createdAt;
}
