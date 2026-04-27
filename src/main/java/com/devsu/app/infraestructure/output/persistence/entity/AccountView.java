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
@Table("account_view")
public class AccountView {
    @Id
    private Long id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private Short status;
    private Long customerId;
    private String customerName;
    private String clientId;
    private String customerIdentification;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
