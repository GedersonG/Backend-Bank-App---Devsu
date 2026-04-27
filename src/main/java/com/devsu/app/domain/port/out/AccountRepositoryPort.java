package com.devsu.app.domain.port.out;

import com.devsu.app.domain.model.Account;
import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountRepositoryPort {
    Mono<PaginatedResult<Account>> findAll(AccountFilterDTO filter, int pageSize, int offset);
    Mono<Account> findByAccountNumber(String accountNumber);
    Mono<Account> save(Account account);
    Mono<Account> update(Account account);
    Mono<Void> deleteByAccountNumber(String accountNumber);
    Mono<Long> resetDailyLimits(BigDecimal dailyLimit);
}