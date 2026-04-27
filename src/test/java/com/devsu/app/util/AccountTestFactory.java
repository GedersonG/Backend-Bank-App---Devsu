package com.devsu.app.util;

import com.devsu.app.domain.model.Account;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.account.AccountResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public class AccountTestFactory {

    public static Account buildAccount() {
        return Account.builder()
                .id(1L)
                .accountNumber("001-100001-00")
                .accountType("Ahorro")
                .balance(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("1000.00"))
                .status((short) 1)
                .clientId("uuid-test-123")
                .customerName("Gederson Guzman")
                .customerIdentification("1004808530")
                .build();
    }

    public static AccountResponseDTO buildResponseDTO() {
        return AccountResponseDTO.builder()
                .accountNumber("001-100001-00")
                .accountType("Ahorro")
                .balance(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("1000.00"))
                .status((short) 1)
                .clientId("uuid-test-123")
                .customerName("Gederson Guzman")
                .customerIdentification("1004808530")
                .build();
    }

    public static AccountRequestDTO buildRequestDTO() {
        return AccountRequestDTO.builder()
                .accountType("AHORRO")
                .balance(new BigDecimal("5000.00"))
                .clientId("uuid-test-123")
                .build();
    }

    public static PageResponseDTO<Account> buildAccountPage() {
        return PageResponseDTO.<Account>builder()
                .content(List.of(buildAccount()))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    public static PageResponseDTO<AccountResponseDTO> buildAccountPageDTO() {
        return PageResponseDTO.<AccountResponseDTO>builder()
                .content(List.of(buildResponseDTO()))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
