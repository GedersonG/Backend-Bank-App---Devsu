package com.devsu.app.infraestructure.input.adapter.rest.advice;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.account.DuplicateAccountNumberException;
import com.devsu.app.domain.exception.account.InactiveAccountException;
import com.devsu.app.domain.exception.account.InvalidBalanceException;
import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.exception.customer.DuplicateClientIdException;
import com.devsu.app.domain.exception.customer.DuplicateIdentificationException;
import com.devsu.app.domain.exception.movements.DailyLimitExceededException;
import com.devsu.app.domain.exception.movements.InsufficientFundsException;
import com.devsu.app.domain.exception.movements.InvalidReversalException;
import com.devsu.app.domain.exception.movements.MovementAlreadyReversedException;
import com.devsu.app.domain.exception.movements.MovementNotFoundException;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomerNotFound(
            CustomerNotFoundException ex, ServerWebExchange exchange
    ) {
        log.warn("Customer not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(DuplicateIdentificationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateIdentification(
            DuplicateIdentificationException ex, ServerWebExchange exchange
    ) {
        log.warn("Duplicate identification: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            WebExchangeBindException ex, ServerWebExchange exchange
    ) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("Validation failed: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError("VALIDATION_ERROR", "Los datos enviados no son válidos", details, exchange));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericError(
            Exception ex, ServerWebExchange exchange
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError("INTERNAL_ERROR", "Error interno del servidor", null, exchange));
    }

    @ExceptionHandler(DuplicateClientIdException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateClientId(
            DuplicateClientIdException ex, ServerWebExchange exchange
    ) {
        log.warn("Duplicate clientId: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, ServerWebExchange exchange
    ) {
        log.error("Unhandled data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError("DATA_INTEGRITY_ERROR", "Violación de integridad de datos", null, exchange));
    }

    @ExceptionHandler(DuplicateAccountNumberException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateAccountNumber(
            DuplicateAccountNumberException ex, ServerWebExchange exchange
    ) {
        log.warn("Duplicate account number: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountNotFound(
            AccountNotFoundException ex, ServerWebExchange exchange
    ) {
        log.warn("Account not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(InvalidBalanceException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidBalance(
            InvalidBalanceException ex, ServerWebExchange exchange
    ) {
        log.warn("Invalid balance: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponseDTO> handleInactiveAccount(
            InactiveAccountException ex, ServerWebExchange exchange
    ) {
        log.warn("Inactive account: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientFunds(
            InsufficientFundsException ex, ServerWebExchange exchange
    ) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleDailyLimitExceeded(
            DailyLimitExceededException ex, ServerWebExchange exchange
    ) {
        log.warn("Daily limit exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(MovementNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleMovementNotFound(
            MovementNotFoundException ex, ServerWebExchange exchange
    ) {
        log.warn("Movement not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(MovementAlreadyReversedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMovementAlreadyReversed(
            MovementAlreadyReversedException ex, ServerWebExchange exchange
    ) {
        log.warn("Movement already reversed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    @ExceptionHandler(InvalidReversalException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidReversal(
            InvalidReversalException ex, ServerWebExchange exchange
    ) {
        log.warn("Invalid reversal attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getErrorCode(), ex.getMessage(), null, exchange));
    }

    private ErrorResponseDTO buildError(
            String errorCode, String message, List<String> details, ServerWebExchange exchange
    ) {
        return ErrorResponseDTO.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .build();
    }
}
