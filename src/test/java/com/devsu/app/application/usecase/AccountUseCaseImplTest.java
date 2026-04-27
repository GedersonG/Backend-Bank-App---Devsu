package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.model.Account;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.port.out.AccountRepositoryPort;
import com.devsu.app.domain.port.out.CustomerRepositoryPort;
import com.devsu.app.domain.service.AccountNumberGenerator;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountPatchRequestDTO;
import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountUseCaseImplTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private AccountUseCaseImpl accountUseCase;

    private Account account;
    private Customer customer;
    private String accountNumber;
    private String clientId;
    private BigDecimal defaultDailyLimit;

    @BeforeEach
    void setUp() {
        accountNumber = "123456789";
        clientId = "test-client-id";
        defaultDailyLimit = new BigDecimal("1000.00");

        // Inyectar el valor de la propiedad
        ReflectionTestUtils.setField(accountUseCase, "defaultDailyLimit", defaultDailyLimit);

        customer = Customer.builder()
                .id(1L)
                .clientId(clientId)
                .name("John Doe")
                .identification("123456789")
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber(accountNumber)
                .accountType("SAVINGS")
                .balance(new BigDecimal("500.00"))
                .dailyLimit(defaultDailyLimit)
                .status((short) 1)
                .customerId(1L)
                .clientId(clientId)
                .customerName("John Doe")
                .customerIdentification("123456789")
                .build();
    }

    @Test
    void findAll_ShouldReturnPageResponseDTO_WhenAccountsExist() {
        // Given
        AccountFilterDTO filter = new AccountFilterDTO();
        int page = 0;
        int pageSize = 10;
        int offset = 0;

        List<Account> accounts = List.of(account);
        long totalCount = 25L;

        PaginatedResult<Account> paginationResult = new PaginatedResult<>(accounts, totalCount);

        when(accountRepository.findAll(filter, pageSize, offset))
                .thenReturn(Mono.just(paginationResult));

        // When & Then
        StepVerifier.create(accountUseCase.findAll(filter, page, pageSize))
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().size() == 1 &&
                                pageResponse.getPage() == 0 &&
                                pageResponse.getPageSize() == 1 &&
                                pageResponse.getTotalElements() == 25L &&
                                pageResponse.getTotalPages() == 3 &&
                                pageResponse.isHasNext() &&
                                !pageResponse.isHasPrevious())
                .verifyComplete();

        verify(accountRepository, times(1)).findAll(filter, pageSize, offset);
    }

    @Test
    void findAll_ShouldHandleEmptyResult() {
        // Given
        AccountFilterDTO filter = new AccountFilterDTO();
        int page = 0;
        int pageSize = 10;
        int offset = 0;

        PaginatedResult<Account> emptyResult = new PaginatedResult<>(List.of(), 0L);

        when(accountRepository.findAll(filter, pageSize, offset))
                .thenReturn(Mono.just(emptyResult));

        // When & Then
        StepVerifier.create(accountUseCase.findAll(filter, page, pageSize))
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().isEmpty() &&
                                pageResponse.getTotalElements() == 0 &&
                                pageResponse.getTotalPages() == 0 &&
                                !pageResponse.isHasNext() &&
                                !pageResponse.isHasPrevious())
                .verifyComplete();
    }

    @Test
    void createAccount_ShouldCreateAccount_WhenCustomerExists() {
        // Given
        Account newAccount = Account.builder()
                .clientId(clientId)
                .accountType("CHECKING")
                .balance(new BigDecimal("1000.00"))
                .build();

        String generatedAccountNumber = "987654321";

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));
        when(accountNumberGenerator.generate(newAccount.getAccountType()))
                .thenReturn(generatedAccountNumber);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {
                    Account accountToSave = invocation.getArgument(0);
                    assert accountToSave.getCustomerId().equals(customer.getId());
                    assert accountToSave.getAccountNumber().equals(generatedAccountNumber);
                    assert accountToSave.getDailyLimit().equals(defaultDailyLimit);
                    assert accountToSave.getStatus() == 1;
                    assert accountToSave.getBalance().equals(new BigDecimal("1000.00"));
                    return Mono.just(accountToSave);
                });

        // When & Then
        StepVerifier.create(accountUseCase.createAccount(newAccount))
                .expectNextMatches(saved ->
                        saved.getAccountNumber().equals(generatedAccountNumber) &&
                                saved.getDailyLimit().equals(defaultDailyLimit) &&
                                saved.getStatus() == 1)
                .verifyComplete();

        verify(customerRepository, times(1)).findByClientId(clientId);
        verify(accountNumberGenerator, times(1)).generate(newAccount.getAccountType());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldSetBalanceToZero_WhenBalanceIsNull() {
        // Given
        Account newAccount = Account.builder()
                .clientId(clientId)
                .accountType("SAVINGS")
                .balance(null)
                .build();

        String generatedAccountNumber = "987654321";

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));
        when(accountNumberGenerator.generate(newAccount.getAccountType()))
                .thenReturn(generatedAccountNumber);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {
                    Account accountToSave = invocation.getArgument(0);
                    assert accountToSave.getBalance().equals(BigDecimal.ZERO);
                    return Mono.just(accountToSave);
                });

        // When & Then
        StepVerifier.create(accountUseCase.createAccount(newAccount))
                .expectNextMatches(saved -> saved.getBalance().equals(BigDecimal.ZERO))
                .verifyComplete();
    }

    @Test
    void createAccount_ShouldThrowException_WhenCustomerNotFound() {
        // Given
        Account newAccount = Account.builder()
                .clientId(clientId)
                .accountType("SAVINGS")
                .build();

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(accountUseCase.createAccount(newAccount))
                .expectErrorMatches(throwable ->
                        throwable instanceof CustomerNotFoundException)
                .verify();

        verify(customerRepository, times(1)).findByClientId(clientId);
        verify(accountNumberGenerator, never()).generate(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void findByAccountNumber_ShouldReturnAccount_WhenExists() {
        // Given
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.just(account));

        // When & Then
        StepVerifier.create(accountUseCase.findByAccountNumber(accountNumber))
                .expectNextMatches(found ->
                        found.getAccountNumber().equals(accountNumber) &&
                                found.getAccountType().equals("SAVINGS"))
                .verifyComplete();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void findByAccountNumber_ShouldThrowException_WhenNotFound() {
        // Given
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(accountUseCase.findByAccountNumber(accountNumber))
                .expectErrorMatches(throwable ->
                        throwable instanceof AccountNotFoundException)
                .verify();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void deleteByAccountNumber_ShouldDelete_WhenExists() {
        // Given
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.just(account));
        when(accountRepository.deleteByAccountNumber(accountNumber))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(accountUseCase.deleteByAccountNumber(accountNumber))
                .verifyComplete();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
        verify(accountRepository, times(1)).deleteByAccountNumber(accountNumber);
    }

    @Test
    void deleteByAccountNumber_ShouldThrowException_WhenNotFound() {
        // Given
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(accountUseCase.deleteByAccountNumber(accountNumber))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).deleteByAccountNumber(anyString());
    }

    @Test
    void updateAccount_ShouldUpdateAccount_WhenExists() {
        // Given
        Account updatedAccount = Account.builder()
                .accountType("CHECKING")
                .balance(new BigDecimal("1500.00"))
                .status((short) 2)
                .dailyLimit(new BigDecimal("2000.00"))
                .build();

        Account accountAfterApplyUpdate = account.toBuilder()
                .accountType("CHECKING")
                .balance(new BigDecimal("1500.00"))
                .status((short) 2)
                .dailyLimit(new BigDecimal("2000.00"))
                .build();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.just(account));
        when(accountRepository.update(any(Account.class)))
                .thenReturn(Mono.just(accountAfterApplyUpdate));

        // When & Then
        StepVerifier.create(accountUseCase.updateAccount(accountNumber, updatedAccount))
                .expectNextMatches(updated ->
                        updated.getAccountType().equals("CHECKING") &&
                                updated.getBalance().equals(new BigDecimal("1500.00")) &&
                                updated.getStatus() == 2 &&
                                updated.getDailyLimit().equals(new BigDecimal("2000.00")))
                .verifyComplete();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
        verify(accountRepository, times(1)).update(any(Account.class));
    }

    @Test
    void updateAccount_ShouldKeepExistingDailyLimit_WhenIncomingDailyLimitIsNull() {
        // Given
        Account updatedAccount = Account.builder()
                .accountType("CHECKING")
                .balance(new BigDecimal("1500.00"))
                .status((short) 2)
                .dailyLimit(null)  // No enviar daily limit
                .build();

        Account accountAfterApplyUpdate = account.toBuilder()
                .accountType("CHECKING")
                .balance(new BigDecimal("1500.00"))
                .status((short) 2)
                .dailyLimit(defaultDailyLimit)  // Mantener el existente
                .build();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.just(account));
        when(accountRepository.update(any(Account.class)))
                .thenReturn(Mono.just(accountAfterApplyUpdate));

        // When & Then
        StepVerifier.create(accountUseCase.updateAccount(accountNumber, updatedAccount))
                .expectNextMatches(updated ->
                        updated.getDailyLimit().equals(defaultDailyLimit))
                .verifyComplete();

        verify(accountRepository, times(1)).update(any(Account.class));
    }

    @Test
    void updateAccount_ShouldThrowException_WhenNotFound() {
        // Given
        Account updatedAccount = Account.builder().build();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(accountUseCase.updateAccount(accountNumber, updatedAccount))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).update(any(Account.class));
    }

    @Test
    void patchAccount_ShouldApplyPatch_WhenExists() {
        // Given
        AccountPatchRequestDTO patch = AccountPatchRequestDTO.builder()
                .accountType("CHECKING")
                .balance(new BigDecimal("2000.00"))
                .dailyLimit(new BigDecimal("3000.00"))
                .status((short) 2)
                .build();

        Account accountAfterPatch = account.toBuilder()
                .accountType("CHECKING")
                .balance(new BigDecimal("2000.00"))
                .dailyLimit(new BigDecimal("3000.00"))
                .status((short) 2)
                .build();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.just(account));
        when(accountRepository.update(any(Account.class)))
                .thenReturn(Mono.just(accountAfterPatch));

        // When & Then
        StepVerifier.create(accountUseCase.patchAccount(accountNumber, patch))
                .expectNextMatches(patched ->
                        patched.getAccountType().equals("CHECKING") &&
                                patched.getBalance().equals(new BigDecimal("2000.00")) &&
                                patched.getDailyLimit().equals(new BigDecimal("3000.00")) &&
                                patched.getStatus() == 2)
                .verifyComplete();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
        verify(accountRepository, times(1)).update(any(Account.class));
    }

    @Test
    void patchAccount_ShouldOnlyUpdateProvidedFields_WhenPartialPatch() {
        // Given
        AccountPatchRequestDTO patch = AccountPatchRequestDTO.builder()
                .balance(new BigDecimal("2500.00"))
                .build();

        Account accountAfterPatch = account.toBuilder()
                .balance(new BigDecimal("2500.00"))
                .build();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.just(account));
        when(accountRepository.update(any(Account.class)))
                .thenReturn(Mono.just(accountAfterPatch));

        // When & Then
        StepVerifier.create(accountUseCase.patchAccount(accountNumber, patch))
                .expectNextMatches(patched ->
                        patched.getBalance().equals(new BigDecimal("2500.00")) &&
                                patched.getAccountType().equals("SAVINGS") &&  // Debe mantener el original
                                patched.getDailyLimit().equals(defaultDailyLimit))  // Mantener el original
                .verifyComplete();

        verify(accountRepository, times(1)).update(any(Account.class));
    }

    @Test
    void patchAccount_ShouldThrowException_WhenNotFound() {
        // Given
        AccountPatchRequestDTO patch = new AccountPatchRequestDTO();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(accountUseCase.patchAccount(accountNumber, patch))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).update(any(Account.class));
    }

    @Test
    void resetDailyLimits_ShouldResetAllAccountsDailyLimits() {
        // Given
        Long expectedAffectedRows = 15L;

        when(accountRepository.resetDailyLimits(defaultDailyLimit))
                .thenReturn(Mono.just(expectedAffectedRows));

        // When & Then
        StepVerifier.create(accountUseCase.resetDailyLimits())
                .expectNext(expectedAffectedRows)
                .verifyComplete();

        verify(accountRepository, times(1)).resetDailyLimits(defaultDailyLimit);
    }

    @Test
    void resetDailyLimits_ShouldReturnZero_WhenNoAccountsAffected() {
        // Given
        Long expectedAffectedRows = 0L;

        when(accountRepository.resetDailyLimits(defaultDailyLimit))
                .thenReturn(Mono.just(expectedAffectedRows));

        // When & Then
        StepVerifier.create(accountUseCase.resetDailyLimits())
                .expectNext(0L)
                .verifyComplete();

        verify(accountRepository, times(1)).resetDailyLimits(defaultDailyLimit);
    }
}