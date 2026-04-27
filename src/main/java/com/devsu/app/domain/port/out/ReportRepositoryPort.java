package com.devsu.app.domain.port.out;

import com.devsu.app.domain.model.AccountStatement;
import com.devsu.app.domain.model.Customer;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepositoryPort {
    Mono<List<AccountStatement>> getAccountStatements(
            String clientId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Customer> findCustomerByClientId(String clientId);
}
