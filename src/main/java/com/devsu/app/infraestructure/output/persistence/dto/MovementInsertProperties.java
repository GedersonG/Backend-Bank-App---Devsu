package com.devsu.app.infraestructure.output.persistence.dto;

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
public class MovementInsertProperties {
    private LocalDateTime movementDate;
    private String movementType;
    private BigDecimal value;
    private BigDecimal balance;
    private Long accountId;
    private Long referenceAccountId;
    private Long referenceMovementId;
    private String createdBy;
    private LocalDateTime createdAt;
}
