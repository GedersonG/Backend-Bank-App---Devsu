package com.devsu.app.domain.port.in;

import com.devsu.app.domain.model.Account;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;
import reactor.core.publisher.Mono;

public interface AccountUseCase {
    Mono<PageResponseDTO<Account>> findAll(AccountFilterDTO filter, int page, int pageSize);
    Mono<Account> findByAccountNumber(String accountNumber);
    Mono<Account> createAccount(Account account);
    Mono<Account> updateAccount(String accountNumber, Account account);
    Mono<Account> patchAccount(String accountNumber, AccountPatchRequestDTO patch);
    Mono<Void> deleteByAccountNumber(String accountNumber);
    Mono<Long> resetDailyLimits();
}
