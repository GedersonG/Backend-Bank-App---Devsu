package com.devsu.app.domain.model;

import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountPatchRequestDTO;
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
public class Account {
    private Long id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private Short status;
    private Long customerId;
    private String clientId;
    private String customerName;
    private String customerIdentification;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Account applyUpdate(Account incoming) {
        return this.toBuilder()
                .accountType(incoming.getAccountType())
                .balance(incoming.getBalance())
                .status(incoming.getStatus())
                .dailyLimit(incoming.getDailyLimit() != null ? incoming.getDailyLimit() : this.getDailyLimit())
                .build();
    }

    public Account applyPatch(AccountPatchRequestDTO patch) {
        return this.toBuilder()
                .accountType(isPresent(patch.getAccountType()) ? patch.getAccountType() : this.getAccountType())
                .balance(patch.getBalance() != null ? patch.getBalance() : this.getBalance())
                .dailyLimit(patch.getDailyLimit() != null ? patch.getDailyLimit() : this.getDailyLimit())
                .status(patch.getStatus() != null ? patch.getStatus() : this.getStatus())
                .build();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
