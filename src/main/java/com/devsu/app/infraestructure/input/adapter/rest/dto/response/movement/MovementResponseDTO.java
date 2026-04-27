package com.devsu.app.infraestructure.input.adapter.rest.dto.response.movement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MovementResponseDTO {
    private Long id;
    private LocalDateTime movementDate;
    private String movementType;
    private BigDecimal value;
    private BigDecimal balance;
    private String accountNumber;
    private String accountType;
    private String clientId;
    private String customerName;
    private String referenceAccountNumber;
    private String referenceCustomerName;
    private LocalDateTime createdAt;
}
