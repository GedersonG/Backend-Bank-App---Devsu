package com.devsu.app.infraestructure.output.persistence.adapter;

import com.devsu.app.domain.model.Movement;
import com.devsu.app.domain.port.out.MovementRepositoryPort;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import com.devsu.app.infraestructure.output.persistence.mapper.MovementPersistenceMapper;
import com.devsu.app.infraestructure.output.persistence.querie.MovementQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MovementPersistenceAdapter implements MovementRepositoryPort {

    private final DatabaseClient databaseClient;
    private final MovementPersistenceMapper mapper;

    @Override
    @SuppressWarnings("SqlSourceToSinkFlow")
    public Mono<PaginatedResult<Movement>> findAll(MovementFilterDTO filter, int pageSize, int offset) {
        String sql = MovementQueries.findAllFiltered(filter);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("limit", pageSize)
                .bind("offset", offset);

        if (filter.getAccountNumber() != null && !filter.getAccountNumber().isBlank()) {
            spec = spec.bind("accountNumber", "%" + filter.getAccountNumber() + "%");
        }
        if (filter.getStartDate() != null) {
            spec = spec.bind("startDate", filter.getStartDate());
        }
        if (filter.getEndDate() != null) {
            spec = spec.bind("endDate", filter.getEndDate());
        }

        return spec.fetch().all()
                .collectList()
                .map(rows -> {
                    long totalCount = rows.isEmpty() ? 0L
                            : ((Number) rows.getFirst().get("total_count")).longValue();
                    List<Movement> movements = rows.stream()
                            .map(mapper::rowToMovement)
                            .toList();
                    return new PaginatedResult<>(movements, totalCount);
                });
    }

    @Override
    public Mono<Movement> findById(Long id) {
        return databaseClient.sql(MovementQueries.FIND_MOVEMENT_BY_ID)
                .bind("id", id)
                .fetch()
                .one()
                .map(mapper::rowToMovement);
    }

    @Override
    public Mono<Map<String, Object>> findAccountById(Long accountId) {
        return databaseClient.sql(MovementQueries.FIND_ACCOUNT_BY_ID)
                .bind("accountId", accountId)
                .fetch()
                .one();
    }

    @Override
    public Mono<Movement> save(Movement movement) {
        return databaseClient.sql(MovementQueries.INSERT_MOVEMENT)
                .bindProperties(mapper.toInsertProperties(movement))
                .fetch()
                .one()
                .map(row -> {
                    movement.setId((Long) row.get("id"));
                    movement.setMovementDate((LocalDateTime) row.get("movement_date"));
                    return movement;
                });
    }

    @Override
    public Mono<Map<String, Object>> findAccountByNumber(String accountNumber) {
        return databaseClient.sql(MovementQueries.FIND_ACCOUNT_BY_NUMBER)
                .bind("accountNumber", accountNumber)
                .fetch()
                .one();
    }

    @Override
    public Mono<Void> updateAccountBalanceAndLimit(Long accountId, BigDecimal balance, BigDecimal dailyLimit) {
        return databaseClient.sql(MovementQueries.UPDATE_ACCOUNT_BALANCE_AND_LIMIT)
                .bind("balance", balance)
                .bind("dailyLimit", dailyLimit)
                .bind("accountId", accountId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Void> updateAccountBalance(Long accountId, BigDecimal balance) {
        return databaseClient.sql(MovementQueries.UPDATE_ACCOUNT_BALANCE_ONLY)
                .bind("balance", balance)
                .bind("accountId", accountId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
