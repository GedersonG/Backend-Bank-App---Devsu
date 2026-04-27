package com.devsu.app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CustomerStatement {
    private String clientId;
    private String customerName;
    private String customerIdentification;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<AccountStatement> accounts;
    private BigDecimal grandTotalDebits;
    private BigDecimal grandTotalCredits;
}
