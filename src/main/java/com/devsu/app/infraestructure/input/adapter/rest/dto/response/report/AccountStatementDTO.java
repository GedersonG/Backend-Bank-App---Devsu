package com.devsu.app.infraestructure.input.adapter.rest.dto.response.report;

import com.devsu.app.infraestructure.input.adapter.rest.dto.response.movement.MovementResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AccountStatementDTO {
    private String accountNumber;
    private String accountType;
    private BigDecimal currentBalance;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private Long totalMovements;
    private List<MovementResponseDTO> movements;
}
