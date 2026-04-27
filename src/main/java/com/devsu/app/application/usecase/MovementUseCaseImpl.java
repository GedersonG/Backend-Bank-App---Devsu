package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.account.InactiveAccountException;
import com.devsu.app.domain.exception.movements.DailyLimitExceededException;
import com.devsu.app.domain.exception.movements.InsufficientFundsException;
import com.devsu.app.domain.exception.movements.InvalidReversalException;
import com.devsu.app.domain.exception.movements.MovementAlreadyReversedException;
import com.devsu.app.domain.exception.movements.MovementNotFoundException;
import com.devsu.app.domain.model.Movement;
import com.devsu.app.domain.port.in.MovementUseCase;
import com.devsu.app.domain.port.out.MovementRepositoryPort;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MovementUseCaseImpl implements MovementUseCase {

    private final MovementRepositoryPort movementRepository;

    @Override
    public Mono<PageResponseDTO<Movement>> findAll(MovementFilterDTO filter, int page, int pageSize) {
        int offset = page * pageSize;

        return movementRepository.findAll(filter, pageSize, offset)
                .map(result -> {
                    long totalElements = result.totalCount();
                    int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                    return PageResponseDTO.<Movement>builder()
                            .content(result.content())
                            .page(page)
                            .pageSize(result.content().size())
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .hasNext(page < totalPages - 1)
                            .hasPrevious(page > 0)
                            .build();
                });
    }

    @Override
    @Transactional
    public Mono<Movement> reverseMovement(Long movementId) {
        return movementRepository.findById(movementId)
                .switchIfEmpty(Mono.error(new MovementNotFoundException(movementId)))
                .flatMap(original -> {

                    if (!"Credito".equals(original.getMovementType())) {
                        return Mono.error(new InvalidReversalException(movementId));
                    }

                    if ("REVERSAL".equals(original.getCreatedBy())) {
                        return Mono.error(new MovementAlreadyReversedException(movementId));
                    }

                    return movementRepository.findAccountById(original.getAccountId())
                            .flatMap(account -> {
                                BigDecimal currentBalance = (BigDecimal) account.get("balance");

                                BigDecimal newBalance = currentBalance.subtract(original.getValue());

                                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                                    return Mono.error(new InsufficientFundsException(original.getAccountNumber()));
                                }

                                Movement reversal = Movement.builder()
                                        .movementType("Debito")
                                        .value(original.getValue())
                                        .balance(newBalance)
                                        .accountId(original.getAccountId())
                                        .accountNumber(original.getAccountNumber())
                                        .referenceAccountId(original.getReferenceAccountId())
                                        .referenceMovementId(movementId)
                                        .build();

                                if (original.getReferenceAccountId() != null) {
                                    return movementRepository.save(reversal)
                                            .flatMap(saved ->
                                                    restoreSourceAccount(original, saved.getId())
                                                            .then(movementRepository.updateAccountBalance(
                                                                    original.getAccountId(), newBalance))
                                                            .thenReturn(saved)
                                            );
                                }

                                return movementRepository.save(reversal)
                                        .flatMap(saved ->
                                                movementRepository.updateAccountBalance(
                                                                original.getAccountId(), newBalance)
                                                        .thenReturn(saved)
                                        );
                            });
                });
    }

    @Override
    @Transactional
    public Mono<Movement> createMovement(Movement movement) {
        return movementRepository.findAccountByNumber(movement.getAccountNumber())
                .switchIfEmpty(Mono.error(new AccountNotFoundException(movement.getAccountNumber())))
                .flatMap(account -> {
                    short status = ((Number) account.get("status")).shortValue();
                    if (status != 1) {
                        return Mono.error(new InactiveAccountException(movement.getAccountNumber()));
                    }

                    BigDecimal currentBalance = (BigDecimal) account.get("balance");
                    BigDecimal currentDailyLimit = (BigDecimal) account.get("daily_limit");
                    Long accountId = ((Number) account.get("id")).longValue();

                    if ("Debito".equals(movement.getMovementType())) {
                        if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
                            return Mono.error(new InsufficientFundsException(movement.getAccountNumber()));
                        }
                        if (movement.getValue().compareTo(currentDailyLimit) > 0) {
                            return Mono.error(new DailyLimitExceededException(
                                    movement.getAccountNumber(), currentDailyLimit));
                        }
                    }

                    BigDecimal newBalance = calculateBalance(currentBalance, movement);

                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new InsufficientFundsException(movement.getAccountNumber()));
                    }

                    movement.setAccountId(accountId);
                    movement.setBalance(newBalance);

                    if (movement.getReferenceAccountNumber() != null) {
                        return handleTransfer(movement, accountId, newBalance, currentDailyLimit);
                    }

                    return saveMovementAndUpdateAccount(movement, accountId, newBalance, currentDailyLimit);
                });
    }

    private Mono<Movement> handleTransfer(
            Movement movement, Long sourceAccountId,
            BigDecimal newSourceBalance, BigDecimal currentDailyLimit
    ) {
        return movementRepository.findAccountByNumber(movement.getReferenceAccountNumber())
                .switchIfEmpty(Mono.error(
                        new AccountNotFoundException(movement.getReferenceAccountNumber())))
                .flatMap(refAccount -> {
                    short refStatus = ((Number) refAccount.get("status")).shortValue();
                    if (refStatus != 1) {
                        return Mono.error(
                                new InactiveAccountException(movement.getReferenceAccountNumber()));
                    }

                    Long refAccountId = ((Number) refAccount.get("id")).longValue();
                    BigDecimal refCurrentBalance = (BigDecimal) refAccount.get("balance");
                    BigDecimal refNewBalance = refCurrentBalance.add(movement.getValue());

                    movement.setReferenceAccountId(refAccountId);

                    return movementRepository.save(movement)
                            .flatMap(savedDebit -> {
                                Movement creditMovement = Movement.builder()
                                        .accountId(refAccountId)
                                        .accountNumber(movement.getReferenceAccountNumber())
                                        .movementType("Credito")
                                        .value(movement.getValue())
                                        .balance(refNewBalance)
                                        .referenceAccountId(sourceAccountId)
                                        .referenceMovementId(savedDebit.getId())
                                        .build();

                                return movementRepository.save(creditMovement)
                                        .flatMap(savedCredit -> {
                                            savedDebit.setReferenceMovementId(savedCredit.getId());

                                            BigDecimal newDailyLimit = currentDailyLimit.subtract(movement.getValue());
                                            return movementRepository.updateAccountBalanceAndLimit(
                                                            sourceAccountId, newSourceBalance, newDailyLimit)
                                                    .then(movementRepository.updateAccountBalance(
                                                            refAccountId, refNewBalance))
                                                    .thenReturn(savedDebit);
                                        });
                            });
                });
    }

    private Mono<Movement> saveMovementAndUpdateAccount(
            Movement movement, Long accountId,
            BigDecimal newBalance, BigDecimal currentDailyLimit
    ) {
        return movementRepository.save(movement)
                .flatMap(saved -> {
                    if ("Debito".equals(movement.getMovementType())) {
                        BigDecimal newDailyLimit = currentDailyLimit.subtract(movement.getValue());
                        return movementRepository.updateAccountBalanceAndLimit(
                                        accountId, newBalance, newDailyLimit)
                                .thenReturn(saved);
                    }
                    return movementRepository.updateAccountBalance(accountId, newBalance)
                            .thenReturn(saved);
                });
    }

    private BigDecimal calculateBalance(BigDecimal currentBalance, Movement movement) {
        return "Debito".equals(movement.getMovementType())
                ? currentBalance.subtract(movement.getValue())
                : currentBalance.add(movement.getValue());
    }

    private Mono<Void> restoreSourceAccount(Movement original, Long reversalId) {
        return movementRepository.findAccountById(original.getReferenceAccountId())
                .flatMap(sourceAccount -> {
                    BigDecimal sourceBalance = (BigDecimal) sourceAccount.get("balance");
                    BigDecimal sourceDailyLimit = (BigDecimal) sourceAccount.get("daily_limit");

                    BigDecimal sourceNewBalance = sourceBalance.add(original.getValue());

                    boolean wasToday = original.getMovementDate()
                            .toLocalDate()
                            .equals(LocalDate.now());

                    BigDecimal sourceNewDailyLimit = wasToday
                            ? sourceDailyLimit.add(original.getValue())
                            : sourceDailyLimit;

                    Movement sourceReversal = Movement.builder()
                            .movementType("Credito")
                            .value(original.getValue())
                            .balance(sourceNewBalance)
                            .accountId(original.getReferenceAccountId())
                            .referenceAccountId(original.getAccountId())
                            .referenceMovementId(reversalId)
                            .build();

                    return movementRepository.save(sourceReversal)
                            .then(movementRepository.updateAccountBalanceAndLimit(
                                    original.getReferenceAccountId(),
                                    sourceNewBalance,
                                    sourceNewDailyLimit));
                });
    }
}
