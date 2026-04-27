package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.account.InactiveAccountException;
import com.devsu.app.domain.exception.movements.DailyLimitExceededException;
import com.devsu.app.domain.exception.movements.InsufficientFundsException;
import com.devsu.app.domain.exception.movements.InvalidReversalException;
import com.devsu.app.domain.exception.movements.MovementAlreadyReversedException;
import com.devsu.app.domain.exception.movements.MovementNotFoundException;
import com.devsu.app.domain.model.Movement;
import com.devsu.app.domain.port.out.MovementRepositoryPort;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementUseCaseImplTest {

    @Mock
    private MovementRepositoryPort movementRepository;

    @InjectMocks
    private MovementUseCaseImpl movementUseCase;

    private Movement movement;
    private Map<String, Object> accountMap;
    private Long movementId;
    private Long accountId;
    private String accountNumber;
    private String referenceAccountNumber;

    @BeforeEach
    void setUp() {
        movementId = 1L;
        accountId = 100L;
        accountNumber = "123456789";
        referenceAccountNumber = "987654321";

        accountMap = new HashMap<>();
        accountMap.put("id", accountId);
        accountMap.put("accountNumber", accountNumber);
        accountMap.put("balance", new BigDecimal("1000.00"));
        accountMap.put("daily_limit", new BigDecimal("500.00"));
        accountMap.put("status", (short) 1);
        accountMap.put("accountType", "SAVINGS");

        movement = Movement.builder()
                .id(movementId)
                .movementType("Debito")
                .value(new BigDecimal("100.00"))
                .balance(new BigDecimal("900.00"))
                .accountId(accountId)
                .accountNumber(accountNumber)
                .movementDate(LocalDateTime.now())
                .build();
    }

    @Test
    void findAll_ShouldReturnPageResponseDTO_WhenMovementsExist() {
        // Given
        MovementFilterDTO filter = new MovementFilterDTO();
        int page = 0;
        int pageSize = 10;
        int offset = 0;

        List<Movement> movements = List.of(movement);
        long totalCount = 25L;

        PaginatedResult<Movement> paginationResult = new PaginatedResult<>(movements, totalCount);

        when(movementRepository.findAll(filter, pageSize, offset))
                .thenReturn(Mono.just(paginationResult));

        // When & Then
        StepVerifier.create(movementUseCase.findAll(filter, page, pageSize))
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().size() == 1 &&
                                pageResponse.getPage() == 0 &&
                                pageResponse.getTotalElements() == 25L &&
                                pageResponse.getTotalPages() == 3)
                .verifyComplete();

        verify(movementRepository, times(1)).findAll(filter, pageSize, offset);
    }

    @Test
    void createMovement_ShouldCreateDebitMovement_WhenValid() {
        // Given
        movement.setMovementType("Debito");

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(accountMap));
        when(movementRepository.save(any(Movement.class)))
                .thenReturn(Mono.just(movement));
        when(movementRepository.updateAccountBalanceAndLimit(anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(movement))
                .expectNextMatches(saved ->
                        saved.getMovementType().equals("Debito") &&
                                saved.getValue().equals(new BigDecimal("100.00")) &&
                                saved.getBalance().equals(new BigDecimal("900.00")))
                .verifyComplete();

        verify(movementRepository, times(1)).findAccountByNumber(accountNumber);
        verify(movementRepository, times(1)).save(any(Movement.class));
        verify(movementRepository, times(1)).updateAccountBalanceAndLimit(
                eq(accountId), eq(new BigDecimal("900.00")), eq(new BigDecimal("400.00")));
    }

    @Test
    void createMovement_ShouldCreateCreditMovement_WhenValid() {
        // Given
        Movement credit = Movement.builder()
                .movementType("Credito")
                .value(new BigDecimal("100.00"))
                .accountNumber(accountNumber)
                .build();

        Map<String, Object> account = new HashMap<>(accountMap);

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(account));
        when(movementRepository.save(any(Movement.class)))
                .thenReturn(Mono.just(credit));
        when(movementRepository.updateAccountBalance(anyLong(), any(BigDecimal.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(credit))
                .expectNextMatches(saved ->
                        saved.getMovementType().equals("Credito"))
                .verifyComplete();

        verify(movementRepository, times(1)).updateAccountBalance(
                eq(accountId), eq(new BigDecimal("1100.00")));
        verify(movementRepository, never()).updateAccountBalanceAndLimit(anyLong(), any(), any());
    }

    @Test
    void createMovement_ShouldThrowException_WhenAccountNotFound() {
        // Given
        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(movement))
                .expectErrorMatches(throwable ->
                        throwable instanceof AccountNotFoundException)
                .verify();

        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void createMovement_ShouldThrowException_WhenAccountIsInactive() {
        // Given
        Map<String, Object> inactiveAccount = new HashMap<>(accountMap);
        inactiveAccount.put("status", (short) 0);

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(inactiveAccount));

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(movement))
                .expectErrorMatches(throwable ->
                        throwable instanceof InactiveAccountException)
                .verify();

        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void createMovement_ShouldThrowException_WhenInsufficientFundsForDebit() {
        // Given
        movement.setValue(new BigDecimal("1500.00"));

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(accountMap));

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(movement))
                .expectError(DailyLimitExceededException.class)
                .verify();

        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void createMovement_ShouldThrowException_WhenDailyLimitExceeded() {
        // Given
        movement.setValue(new BigDecimal("600.00"));

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(accountMap));

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(movement))
                .expectErrorMatches(throwable ->
                        throwable instanceof DailyLimitExceededException &&
                                throwable.getMessage().contains("500"))
                .verify();

        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void createMovement_ShouldHandleTransfer_WhenReferenceAccountExists() {
        // Given
        Movement transfer = Movement.builder()
                .movementType("Debito")
                .value(new BigDecimal("100.00"))
                .accountNumber(accountNumber)
                .referenceAccountNumber(referenceAccountNumber)
                .build();

        Map<String, Object> sourceAccount = new HashMap<>(accountMap);
        Map<String, Object> targetAccount = new HashMap<>();
        targetAccount.put("id", 200L);
        targetAccount.put("accountNumber", referenceAccountNumber);
        targetAccount.put("balance", new BigDecimal("500.00"));
        targetAccount.put("daily_limit", new BigDecimal("1000.00"));
        targetAccount.put("status", (short) 1);

        Movement savedDebit = Movement.builder()
                .id(1L)
                .movementType("Debito")
                .value(new BigDecimal("100.00"))
                .build();

        Movement savedCredit = Movement.builder()
                .id(2L)
                .movementType("Credito")
                .value(new BigDecimal("100.00"))
                .build();

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(sourceAccount));
        when(movementRepository.findAccountByNumber(referenceAccountNumber))
                .thenReturn(Mono.just(targetAccount));
        when(movementRepository.save(any(Movement.class)))
                .thenReturn(Mono.just(savedDebit))
                .thenReturn(Mono.just(savedCredit));
        when(movementRepository.updateAccountBalanceAndLimit(anyLong(), any(), any()))
                .thenReturn(Mono.empty());
        when(movementRepository.updateAccountBalance(anyLong(), any()))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(transfer))
                .expectNextMatches(saved -> true)
                .verifyComplete();

        verify(movementRepository, times(2)).save(any(Movement.class));
        verify(movementRepository, times(1)).updateAccountBalanceAndLimit(anyLong(), any(), any());
        verify(movementRepository, times(1)).updateAccountBalance(anyLong(), any());
    }

    @Test
    void createMovement_ShouldThrowException_WhenTransferTargetAccountNotFound() {
        // Given
        Movement transfer = Movement.builder()
                .movementType("Debito")
                .value(new BigDecimal("100.00"))
                .accountNumber(accountNumber)
                .referenceAccountNumber(referenceAccountNumber)
                .build();

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(accountMap));
        when(movementRepository.findAccountByNumber(referenceAccountNumber))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(transfer))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void reverseMovement_ShouldReverseCreditMovement_WhenValid() {
        // Given
        Movement originalCredit = Movement.builder()
                .id(movementId)
                .movementType("Credito")
                .value(new BigDecimal("100.00"))
                .accountId(accountId)
                .accountNumber(accountNumber)
                .movementDate(LocalDateTime.now())
                .createdBy("USER")
                .build();

        Map<String, Object> account = new HashMap<>(accountMap);
        account.put("balance", new BigDecimal("1100.00"));

        Movement reversal = Movement.builder()
                .movementType("Debito")
                .value(new BigDecimal("100.00"))
                .balance(new BigDecimal("1000.00"))
                .build();

        when(movementRepository.findById(movementId))
                .thenReturn(Mono.just(originalCredit));
        when(movementRepository.findAccountById(accountId))
                .thenReturn(Mono.just(account));
        when(movementRepository.save(any(Movement.class)))
                .thenReturn(Mono.just(reversal));
        when(movementRepository.updateAccountBalance(anyLong(), any(BigDecimal.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.reverseMovement(movementId))
                .expectNextMatches(reversed ->
                        reversed.getMovementType().equals("Debito"))
                .verifyComplete();

        verify(movementRepository, times(1)).findById(movementId);
        verify(movementRepository, times(1)).save(any(Movement.class));
        verify(movementRepository, times(1)).updateAccountBalance(eq(accountId), eq(new BigDecimal("1000.00")));
    }

    @Test
    void reverseMovement_ShouldThrowException_WhenMovementNotFound() {
        // Given
        when(movementRepository.findById(movementId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.reverseMovement(movementId))
                .expectError(MovementNotFoundException.class)
                .verify();
    }

    @Test
    void reverseMovement_ShouldThrowException_WhenMovementIsNotCredit() {
        // Given
        movement.setMovementType("Debito");

        when(movementRepository.findById(movementId))
                .thenReturn(Mono.just(movement));

        // When & Then
        StepVerifier.create(movementUseCase.reverseMovement(movementId))
                .expectError(InvalidReversalException.class)
                .verify();
    }

    @Test
    void reverseMovement_ShouldThrowException_WhenMovementAlreadyReversed() {
        // Given
        Movement alreadyReversed = Movement.builder()
                .id(movementId)
                .movementType("Credito")
                .createdBy("REVERSAL")
                .build();

        when(movementRepository.findById(movementId))
                .thenReturn(Mono.just(alreadyReversed));

        // When & Then
        StepVerifier.create(movementUseCase.reverseMovement(movementId))
                .expectError(MovementAlreadyReversedException.class)
                .verify();
    }

    @Test
    void reverseMovement_ShouldThrowException_WhenInsufficientFundsForReversal() {
        // Given
        Movement originalCredit = Movement.builder()
                .id(movementId)
                .movementType("Credito")
                .value(new BigDecimal("100.00"))
                .accountId(accountId)
                .accountNumber(accountNumber)
                .movementDate(LocalDateTime.now())
                .createdBy("USER")
                .build();

        Map<String, Object> account = new HashMap<>(accountMap);
        account.put("balance", new BigDecimal("50.00")); // Balance insuficiente

        when(movementRepository.findById(movementId))
                .thenReturn(Mono.just(originalCredit));
        when(movementRepository.findAccountById(accountId))
                .thenReturn(Mono.just(account));

        // When & Then
        StepVerifier.create(movementUseCase.reverseMovement(movementId))
                .expectError(InsufficientFundsException.class)
                .verify();

        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void reverseMovement_ShouldHandleTransferReversal_WhenHasReferenceAccount() {
        // Given
        LocalDateTime movementDate = LocalDateTime.now();

        Movement originalTransfer = Movement.builder()
                .id(movementId)
                .movementType("Credito")
                .value(new BigDecimal("100.00"))
                .accountId(accountId)
                .accountNumber(accountNumber)
                .referenceAccountId(200L)
                .referenceAccountNumber(referenceAccountNumber)
                .movementDate(movementDate)
                .createdBy("USER")
                .build();

        Map<String, Object> destinationAccount = new HashMap<>(accountMap);
        destinationAccount.put("balance", new BigDecimal("1100.00"));
        destinationAccount.put("daily_limit", new BigDecimal("600.00"));
        destinationAccount.put("id", accountId);

        Map<String, Object> sourceAccount = new HashMap<>();
        sourceAccount.put("balance", new BigDecimal("400.00"));
        sourceAccount.put("daily_limit", new BigDecimal("500.00"));
        sourceAccount.put("id", 200L);

        when(movementRepository.findById(movementId))
                .thenReturn(Mono.just(originalTransfer));
        when(movementRepository.findAccountById(accountId))
                .thenReturn(Mono.just(destinationAccount));
        when(movementRepository.findAccountById(200L))
                .thenReturn(Mono.just(sourceAccount));
        when(movementRepository.save(any(Movement.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(movementRepository.updateAccountBalance(anyLong(), any(BigDecimal.class)))
                .thenReturn(Mono.empty());
        when(movementRepository.updateAccountBalanceAndLimit(anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.reverseMovement(movementId))
                .expectNextCount(1)
                .verifyComplete();

        verify(movementRepository, times(1))
                .updateAccountBalance(eq(accountId), any(BigDecimal.class));

        verify(movementRepository, times(1))
                .updateAccountBalanceAndLimit(eq(200L), any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void reverseMovement_ShouldRestoreDailyLimit_WhenReversalOnSameDay() {
        // Given
        Movement originalCredit = Movement.builder()
                .id(movementId)
                .movementType("Credito")
                .value(new BigDecimal("100.00"))
                .accountId(accountId)
                .accountNumber(accountNumber)
                .movementDate(LocalDateTime.now())
                .createdBy("USER")
                .build();

        Map<String, Object> account = new HashMap<>(accountMap);
        account.put("balance", new BigDecimal("1100.00"));
        account.put("daily_limit", new BigDecimal("400.00")); // Límite reducido

        when(movementRepository.findById(movementId))
                .thenReturn(Mono.just(originalCredit));
        when(movementRepository.findAccountById(accountId))
                .thenReturn(Mono.just(account));
        when(movementRepository.save(any(Movement.class)))
                .thenReturn(Mono.just(movement));
        when(movementRepository.updateAccountBalance(anyLong(), any(BigDecimal.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(movementUseCase.reverseMovement(movementId))
                .expectNextCount(1)
                .verifyComplete();

        verify(movementRepository, times(1)).updateAccountBalance(
                eq(accountId), eq(new BigDecimal("1000.00")));
    }

    @Test
    void createMovement_ShouldThrowException_WhenDebitOnZeroBalance() {
        // Given
        Map<String, Object> zeroBalanceAccount = new HashMap<>(accountMap);
        zeroBalanceAccount.put("balance", BigDecimal.ZERO);

        when(movementRepository.findAccountByNumber(accountNumber))
                .thenReturn(Mono.just(zeroBalanceAccount));

        // When & Then
        StepVerifier.create(movementUseCase.createMovement(movement))
                .expectError(InsufficientFundsException.class)
                .verify();
    }
}