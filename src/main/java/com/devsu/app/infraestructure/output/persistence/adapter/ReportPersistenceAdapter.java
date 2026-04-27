package com.devsu.app.infraestructure.output.persistence.adapter;

import com.devsu.app.domain.model.AccountStatement;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.port.out.ReportRepositoryPort;
import com.devsu.app.infraestructure.output.persistence.mapper.CustomerPersistenceMapper;
import com.devsu.app.infraestructure.output.persistence.querie.ReportQueries;
import com.devsu.app.infraestructure.output.persistence.repository.CustomerViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements ReportRepositoryPort {

    private final DatabaseClient databaseClient;
    private final CustomerViewRepository customerViewRepository;
    private final CustomerPersistenceMapper customerMapper;

    @Override
    public Mono<List<AccountStatement>> getAccountStatements(
            String clientId, LocalDateTime startDate, LocalDateTime endDate
    ) {
        return databaseClient.sql(ReportQueries.ACCOUNT_STATEMENT)
                .bind("clientId", clientId)
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .fetch()
                .all()
                .map(this::rowToAccountStatement)
                .collectList();
    }

    @Override
    public Mono<Customer> findCustomerByClientId(String clientId) {
        return customerViewRepository.findByClientId(clientId)
                .map(customerMapper::customerViewtoCustomer);
    }

    private AccountStatement rowToAccountStatement(Map<String, Object> row) {
        return AccountStatement.builder()
                .accountNumber((String) row.get("account_number"))
                .accountType((String) row.get("account_type"))
                .currentBalance((BigDecimal) row.get("current_balance"))
                .totalDebits((BigDecimal) row.get("total_debits"))
                .totalCredits((BigDecimal) row.get("total_credits"))
                .totalMovements(((Number) row.get("total_movements")).longValue())
                .movements(new ArrayList<>())
                .build();
    }
}
