package com.devsu.app.infraestructure.output.persistence.adapter;

import com.devsu.app.domain.exception.account.DuplicateAccountNumberException;
import com.devsu.app.domain.exception.account.InvalidBalanceException;
import com.devsu.app.domain.model.Account;
import com.devsu.app.domain.port.out.AccountRepositoryPort;
import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import com.devsu.app.infraestructure.output.persistence.mapper.AccountPersistenceMapper;
import com.devsu.app.infraestructure.output.persistence.querie.AccountQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final DatabaseClient databaseClient;
    private final AccountPersistenceMapper mapper;

    @Override
    public Mono<PaginatedResult<Account>> findAll(AccountFilterDTO filter, int pageSize, int offset) {
        String sql = AccountQueries.findAllFiltered(filter);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("limit", pageSize)
                .bind("offset", offset);

        if (filter.getAccountNumber() != null && !filter.getAccountNumber().isBlank()) {
            spec = spec.bind("accountNumber", "%" + filter.getAccountNumber() + "%");
        }
        if (filter.getAccountType() != null && !filter.getAccountType().isBlank()) {
            spec = spec.bind("accountType", "%" + filter.getAccountType() + "%");
        }
        if (filter.getCustomerIdentification() != null && !filter.getCustomerIdentification().isBlank()) {
            spec = spec.bind("customerIdentification", "%" + filter.getCustomerIdentification() + "%");
        }

        return spec.fetch().all()
                .collectList()
                .map(rows -> {
                    long totalCount = rows.isEmpty() ? 0L : ((Number) rows.getFirst().get("total_count")).longValue();
                    List<Account> accounts = rows.stream()
                            .map(this::rowToAccount)
                            .toList();
                    return new PaginatedResult<>(accounts, totalCount);
                });
    }

    @Override
    public Mono<Long> resetDailyLimits(BigDecimal dailyLimit) {
        return databaseClient.sql(AccountQueries.RESET_DAILY_LIMIT)
                .bind("dailyLimit", dailyLimit)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Account> save(Account account) {
        return databaseClient.sql(AccountQueries.INSERT_ACCOUNT)
                .bindProperties(mapper.toInsertProperties(account))
                .fetch()
                .one()
                .map(row -> {
                    account.setId((Long) row.get("id"));
                    account.setCreatedAt((LocalDateTime) row.get("created_at"));
                    account.setDailyLimit((BigDecimal) row.get("daily_limit"));
                    return account;
                })
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                    if (message.contains("idx_account_number")) {
                        return new DuplicateAccountNumberException(account.getAccountNumber());
                    }
                    return ex;
                });
    }

    @Override
    public Mono<Account> findByAccountNumber(String accountNumber) {
        return databaseClient.sql(AccountQueries.FIND_BY_ACCOUNT_NUMBER)
                .bind("accountNumber", accountNumber)
                .fetch()
                .one()
                .map(this::rowToAccount);
    }

    @Override
    public Mono<Void> deleteByAccountNumber(String accountNumber) {
        return databaseClient.sql(AccountQueries.DEACTIVATE_BY_ACCOUNT_NUMBER)
                .bind("accountNumber", accountNumber)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Account> update(Account account) {
        return databaseClient.sql(AccountQueries.UPDATE_ACCOUNT)
                .bind("accountNumber", account.getAccountNumber())
                .bind("accountType", account.getAccountType())
                .bind("balance", account.getBalance())
                .bind("status", account.getStatus())
                .bind("dailyLimit", account.getDailyLimit())
                .bind("updatedBy", "BACKEND_APP")
                .bind("updatedAt", LocalDateTime.now())
                .fetch()
                .one()
                .map(row -> {
                    account.setId((Long) row.get("id"));
                    return account;
                })
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                    if (message.contains("chk_balance_positive")) {
                        return new InvalidBalanceException(account.getBalance());
                    }
                    return ex;
                });
    }

    private Account rowToAccount(Map<String, Object> row) {
        return Account.builder()
                .id((Long) row.get("id"))
                .accountNumber((String) row.get("account_number"))
                .accountType((String) row.get("account_type"))
                .balance((BigDecimal) row.get("balance"))
                .dailyLimit((BigDecimal) row.get("daily_limit"))
                .status(((Short) row.get("status")))
                .customerId((Long) row.get("customer_id"))
                .clientId((String) row.get("client_id"))
                .customerName((String) row.get("customer_name"))
                .customerIdentification((String) row.get("customer_identification"))
                .createdAt((LocalDateTime) row.get("created_at"))
                .updatedAt((LocalDateTime) row.get("updated_at"))
                .build();
    }
}
