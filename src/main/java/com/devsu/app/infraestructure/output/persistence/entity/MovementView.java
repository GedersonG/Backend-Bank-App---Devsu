package com.devsu.app.infraestructure.output.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("movement_view")
public class MovementView {
    @Id
    private Long id;
    private LocalDateTime movementDate;
    private String movementType;
    private BigDecimal value;
    private BigDecimal balance;
    private Long accountId;
    private String accountNumber;
    private String accountType;
    private String customerName;
    private String customerIdentification;
    private String clientId;
    private Long referenceAccountId;
    private Long referenceMovementId;
    private String referenceAccountNumber;
    private String referenceCustomerName;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
