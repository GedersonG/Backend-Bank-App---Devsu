package com.devsu.app.infraestructure.input.adapter.rest.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CustomerStatementResponseDTO {
    private String clientId;
    private String customerName;
    private String customerIdentification;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<AccountStatementDTO> accounts;
    private BigDecimal grandTotalDebits;
    private BigDecimal grandTotalCredits;
}
