package com.devsu.app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Movement {
    private Long id;
    private LocalDateTime movementDate;
    private String movementType;
    private BigDecimal value;
    private BigDecimal balance;

    private Long accountId;
    private String accountNumber;
    private String accountType;

    private Long referenceAccountId;
    private Long referenceMovementId;
    private String referenceAccountNumber;
    private String referenceCustomerName;

    private String customerName;
    private String customerIdentification;
    private String clientId;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
