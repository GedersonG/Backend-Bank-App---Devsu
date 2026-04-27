package com.devsu.app.domain.port.out;

import com.devsu.app.domain.model.Movement;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

public interface MovementRepositoryPort {
    Mono<Movement> save(Movement movement);
    Mono<Movement> findById(Long id);
    Mono<Map<String, Object>> findAccountByNumber(String accountNumber);
    Mono<Map<String, Object>> findAccountById(Long accountId);
    Mono<Void> updateAccountBalanceAndLimit(Long accountId, BigDecimal balance, BigDecimal dailyLimit);
    Mono<Void> updateAccountBalance(Long accountId, BigDecimal balance);
    Mono<PaginatedResult<Movement>> findAll(MovementFilterDTO filter, int pageSize, int offset);
}
