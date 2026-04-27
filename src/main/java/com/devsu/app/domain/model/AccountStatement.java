package com.devsu.app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatement {
    private String accountNumber;
    private String accountType;
    private BigDecimal currentBalance;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private Long totalMovements;
    private List<Movement> movements;
}
