package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.model.AccountStatement;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.model.CustomerStatement;
import com.devsu.app.domain.port.in.ReportUseCase;
import com.devsu.app.domain.port.out.MovementRepositoryPort;
import com.devsu.app.domain.port.out.ReportRepositoryPort;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportUseCaseImpl implements ReportUseCase {

    private final ReportRepositoryPort reportRepository;
    private final MovementRepositoryPort movementRepository;

    @Override
    public Mono<CustomerStatement> generateStatement(
            String clientId, LocalDateTime startDate, LocalDateTime endDate
    ) {
        return reportRepository.findCustomerByClientId(clientId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(clientId)))
                .flatMap(customer ->
                        reportRepository.getAccountStatements(clientId, startDate, endDate)
                                .flatMap(accounts ->
                                        enrichWithMovements(accounts, startDate, endDate)
                                                .map(enriched -> buildStatement(
                                                        customer, enriched, startDate, endDate))
                                )
                );
    }

    private Mono<List<AccountStatement>> enrichWithMovements(
            List<AccountStatement> accounts, LocalDateTime startDate, LocalDateTime endDate
    ) {
        return Flux.fromIterable(accounts)
                .flatMap(account -> {
                    MovementFilterDTO filter = MovementFilterDTO.builder()
                            .accountNumber(account.getAccountNumber())
                            .startDate(startDate)
                            .endDate(endDate)
                            .build();
                    return movementRepository.findAll(filter, Integer.MAX_VALUE, 0)
                            .map(result -> {
                                account.setMovements(result.content());
                                return account;
                            });
                })
                .collectList();
    }

    private CustomerStatement buildStatement(
            Customer customer, List<AccountStatement> accounts,
            LocalDateTime startDate, LocalDateTime endDate
    ) {
        BigDecimal grandTotalDebits = accounts.stream()
                .map(AccountStatement::getTotalDebits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotalCredits = accounts.stream()
                .map(AccountStatement::getTotalCredits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerStatement.builder()
                .clientId(customer.getClientId())
                .customerName(customer.getName())
                .customerIdentification(customer.getIdentification())
                .startDate(startDate)
                .endDate(endDate)
                .accounts(accounts)
                .grandTotalDebits(grandTotalDebits)
                .grandTotalCredits(grandTotalCredits)
                .build();
    }
}
