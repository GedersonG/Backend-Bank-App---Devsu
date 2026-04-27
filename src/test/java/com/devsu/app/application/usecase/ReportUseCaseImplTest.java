package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.model.AccountStatement;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.model.Movement;
import com.devsu.app.domain.port.out.MovementRepositoryPort;
import com.devsu.app.domain.port.out.ReportRepositoryPort;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportUseCaseImplTest {

    @Mock
    private ReportRepositoryPort reportRepository;

    @Mock
    private MovementRepositoryPort movementRepository;

    @InjectMocks
    private ReportUseCaseImpl reportUseCase;

    private String clientId;
    private Customer customer;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<AccountStatement> accountStatements;
    private List<Movement> movements;

    @BeforeEach
    void setUp() {
        clientId = "test-client-id";
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59);

        customer = Customer.builder()
                .clientId(clientId)
                .name("John Doe")
                .identification("123456789")
                .build();

        movements = List.of(
                Movement.builder()
                        .id(1L)
                        .movementType("Credito")
                        .value(new BigDecimal("500.00"))
                        .balance(new BigDecimal("1500.00"))
                        .movementDate(LocalDateTime.of(2024, 1, 15, 10, 0, 0))
                        .build(),
                Movement.builder()
                        .id(2L)
                        .movementType("Debito")
                        .value(new BigDecimal("200.00"))
                        .balance(new BigDecimal("1300.00"))
                        .movementDate(LocalDateTime.of(2024, 1, 20, 14, 30, 0))
                        .build()
        );

        AccountStatement accountStatement = AccountStatement.builder()
                .accountNumber("123456789")
                .accountType("SAVINGS")
                .totalDebits(new BigDecimal("200.00"))
                .totalCredits(new BigDecimal("500.00"))
                .movements(new ArrayList<>())
                .build();

        accountStatements = List.of(accountStatement);
    }

    @Test
    void generateStatement_ShouldReturnCustomerStatement_WhenCustomerExists() {
        // Given
        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(accountStatements));

        PaginatedResult<Movement> paginationResult = new PaginatedResult<>(movements, movements.size());

        when(movementRepository.findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0)))
                .thenReturn(Mono.just(paginationResult));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectNextMatches(statement ->
                        statement.getClientId().equals(clientId) &&
                                statement.getCustomerName().equals("John Doe") &&
                                statement.getCustomerIdentification().equals("123456789") &&
                                statement.getStartDate().equals(startDate) &&
                                statement.getEndDate().equals(endDate) &&
                                statement.getAccounts().size() == 1 &&
                                statement.getGrandTotalDebits().equals(new BigDecimal("200.00")) &&
                                statement.getGrandTotalCredits().equals(new BigDecimal("500.00")))
                .verifyComplete();

        verify(reportRepository, times(1)).findCustomerByClientId(clientId);
        verify(reportRepository, times(1)).getAccountStatements(clientId, startDate, endDate);
        verify(movementRepository, times(1))
                .findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0));
    }

    @Test
    void generateStatement_ShouldThrowException_WhenCustomerNotFound() {
        // Given
        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectErrorMatches(throwable ->
                        throwable instanceof CustomerNotFoundException)
                .verify();

        verify(reportRepository, times(1)).findCustomerByClientId(clientId);
        verify(reportRepository, never()).getAccountStatements(anyString(), any(), any());
        verify(movementRepository, never()).findAll(any(), anyInt(), anyInt());
    }

    @Test
    void generateStatement_ShouldHandleEmptyAccounts() {
        // Given
        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(new ArrayList<>()));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectNextMatches(statement ->
                        statement.getAccounts().isEmpty() &&
                                statement.getGrandTotalDebits().equals(BigDecimal.ZERO) &&
                                statement.getGrandTotalCredits().equals(BigDecimal.ZERO))
                .verifyComplete();

        verify(reportRepository, times(1)).getAccountStatements(clientId, startDate, endDate);
        verify(movementRepository, never()).findAll(any(), anyInt(), anyInt());
    }

    @Test
    void generateStatement_ShouldEnrichAccountsWithMovements() {
        // Given
        AccountStatement accountWithoutMovements = AccountStatement.builder()
                .accountNumber("123456789")
                .accountType("SAVINGS")
                .totalDebits(new BigDecimal("200.00"))
                .totalCredits(new BigDecimal("500.00"))
                .movements(new ArrayList<>())
                .build();

        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(List.of(accountWithoutMovements)));

        PaginatedResult<Movement> paginationResult = new PaginatedResult<>(movements, movements.size());

        when(movementRepository.findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0)))
                .thenReturn(Mono.just(paginationResult));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectNextMatches(statement -> {
                    AccountStatement enrichedAccount = statement.getAccounts().getFirst();
                    return enrichedAccount.getMovements() != null &&
                            enrichedAccount.getMovements().size() == 2 &&
                            enrichedAccount.getMovements().get(0).getId().equals(1L) &&
                            enrichedAccount.getMovements().get(1).getId().equals(2L);
                })
                .verifyComplete();

        ArgumentCaptor<MovementFilterDTO> filterCaptor = ArgumentCaptor.forClass(MovementFilterDTO.class);
        verify(movementRepository, times(1)).findAll(filterCaptor.capture(), eq(Integer.MAX_VALUE), eq(0));

        MovementFilterDTO capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.getAccountNumber()).isEqualTo("123456789");
        assertThat(capturedFilter.getStartDate()).isEqualTo(startDate);
        assertThat(capturedFilter.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void generateStatement_ShouldHandleMultipleAccounts() {
        // Given
        AccountStatement account1 = AccountStatement.builder()
                .accountNumber("123456789")
                .accountType("SAVINGS")
                .totalDebits(new BigDecimal("200.00"))
                .totalCredits(new BigDecimal("500.00"))
                .movements(new ArrayList<>())
                .build();

        AccountStatement account2 = AccountStatement.builder()
                .accountNumber("987654321")
                .accountType("CHECKING")
                .totalDebits(new BigDecimal("100.00"))
                .totalCredits(new BigDecimal("300.00"))
                .movements(new ArrayList<>())
                .build();

        List<AccountStatement> multipleAccounts = List.of(account1, account2);

        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(multipleAccounts));

        PaginatedResult<Movement> emptyResult = new PaginatedResult<>(new ArrayList<>(), 0);
        when(movementRepository.findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0)))
                .thenReturn(Mono.just(emptyResult));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectNextMatches(statement ->
                        statement.getAccounts().size() == 2 &&
                                statement.getGrandTotalDebits().equals(new BigDecimal("300.00")) && // 200 + 100
                                statement.getGrandTotalCredits().equals(new BigDecimal("800.00")))  // 500 + 300
                .verifyComplete();

        verify(movementRepository, times(2)).findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0));
    }

    @Test
    void generateStatement_ShouldHandleAccountsWithoutMovements() {
        // Given
        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(accountStatements));

        PaginatedResult<Movement> emptyResult = new PaginatedResult<>(new ArrayList<>(), 0);
        when(movementRepository.findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0)))
                .thenReturn(Mono.just(emptyResult));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectNextMatches(statement -> {
                    AccountStatement account = statement.getAccounts().getFirst();
                    return account.getMovements() != null && account.getMovements().isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void generateStatement_ShouldCalculateGrandTotalsCorrectly_WithMixedDebitsAndCredits() {
        // Given
        AccountStatement account1 = AccountStatement.builder()
                .totalDebits(new BigDecimal("150.75"))
                .totalCredits(new BigDecimal("250.50"))
                .build();

        AccountStatement account2 = AccountStatement.builder()
                .totalDebits(new BigDecimal("75.25"))
                .totalCredits(new BigDecimal("125.00"))
                .build();

        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(List.of(account1, account2)));

        PaginatedResult<Movement> emptyResult = new PaginatedResult<>(new ArrayList<>(), 0);
        when(movementRepository.findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0)))
                .thenReturn(Mono.just(emptyResult));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectNextMatches(statement ->
                        statement.getGrandTotalDebits().equals(new BigDecimal("226.00")) && // 150.75 + 75.25
                                statement.getGrandTotalCredits().equals(new BigDecimal("375.50")))  // 250.50 + 125.00
                .verifyComplete();
    }

    @Test
    void generateStatement_ShouldPreserveAccountDetails() {
        // Given
        AccountStatement expectedAccount = AccountStatement.builder()
                .accountNumber("123456789")
                .accountType("SAVINGS")
                .totalDebits(new BigDecimal("200.00"))
                .totalCredits(new BigDecimal("500.00"))
                .movements(new ArrayList<>())
                .build();

        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(List.of(expectedAccount)));

        PaginatedResult<Movement> emptyResult = new PaginatedResult<>(new ArrayList<>(), 0);
        when(movementRepository.findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0)))
                .thenReturn(Mono.just(emptyResult));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectNextMatches(statement -> {
                    AccountStatement actualAccount = statement.getAccounts().getFirst();
                    return actualAccount.getAccountNumber().equals("123456789") &&
                            actualAccount.getAccountType().equals("SAVINGS") &&
                            actualAccount.getTotalDebits().equals(new BigDecimal("200.00")) &&
                            actualAccount.getTotalCredits().equals(new BigDecimal("500.00"));
                })
                .verifyComplete();
    }

    @Test
    void generateStatement_ShouldHandleError_WhenMovementRepositoryFails() {
        // Given
        when(reportRepository.findCustomerByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(reportRepository.getAccountStatements(clientId, startDate, endDate))
                .thenReturn(Mono.just(accountStatements));

        when(movementRepository.findAll(any(MovementFilterDTO.class), eq(Integer.MAX_VALUE), eq(0)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(reportUseCase.generateStatement(clientId, startDate, endDate))
                .expectError(RuntimeException.class)
                .verify();

        verify(reportRepository, times(1)).findCustomerByClientId(clientId);
        verify(reportRepository, times(1)).getAccountStatements(clientId, startDate, endDate);
        verify(movementRepository, times(1)).findAll(any(), anyInt(), anyInt());
    }
}