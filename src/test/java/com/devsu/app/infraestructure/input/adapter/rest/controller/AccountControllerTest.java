package com.devsu.app.infraestructure.input.adapter.rest.controller;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.model.Account;
import com.devsu.app.domain.port.in.AccountUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.account.AccountResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.AccountMapper;
import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;
import com.devsu.app.util.AccountTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountUseCase accountUseCase;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountController accountController;

    // ── POST /v1/cuentas ─────────────────────────────────────────

    @Test
    void createAccount_shouldCallUseCaseAndReturnCreated() {
        AccountRequestDTO request = AccountTestFactory.buildRequestDTO();
        Account account = AccountTestFactory.buildAccount();
        AccountResponseDTO response = AccountTestFactory.buildResponseDTO();

        when(accountMapper.toDomain(request)).thenReturn(account);
        when(accountUseCase.createAccount(account)).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(account)).thenReturn(response);

        StepVerifier.create(accountController.createAccount(request))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(responseEntity.getBody()).isEqualTo(response);
                })
                .verifyComplete();

        verify(accountMapper).toDomain(request);
        verify(accountUseCase).createAccount(account);
        verify(accountMapper).toResponse(account);
    }

    @Test
    void createAccount_shouldPropagateError_whenUseCaseThrows() {
        AccountRequestDTO request = AccountTestFactory.buildRequestDTO();
        Account account = AccountTestFactory.buildAccount();

        when(accountMapper.toDomain(request)).thenReturn(account);
        when(accountUseCase.createAccount(account))
                .thenReturn(Mono.error(new CustomerNotFoundException("uuid-test-123")));

        StepVerifier.create(accountController.createAccount(request))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(accountUseCase).createAccount(account);
    }

    // ── GET /v1/cuentas ──────────────────────────────────────────

    @Test
    void getAllAccounts_shouldCallUseCaseWithFiltersAndReturnOk() {
        AccountFilterDTO filter = AccountFilterDTO.builder()
                .accountNumber("001")
                .accountType("AHORRO")
                .customerIdentification(null)
                .build();

        PageResponseDTO<Account> page = AccountTestFactory.buildAccountPage();
        PageResponseDTO<AccountResponseDTO> pageDTO = AccountTestFactory.buildAccountPageDTO();

        when(accountUseCase.findAll(any(AccountFilterDTO.class), eq(0), eq(20)))
                .thenReturn(Mono.just(page));
        when(accountMapper.toPageResponse(page)).thenReturn(pageDTO);

        StepVerifier.create(accountController.getAllAccounts(0, 20, "001", "AHORRO", null))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();

        verify(accountUseCase).findAll(any(AccountFilterDTO.class), eq(0), eq(20));
        verify(accountMapper).toPageResponse(page);
    }

    // ── GET /v1/cuentas/{accountNumber} ──────────────────────────

    @Test
    void findByAccountNumber_shouldCallUseCaseAndReturnOk() {
        Account account = AccountTestFactory.buildAccount();
        AccountResponseDTO response = AccountTestFactory.buildResponseDTO();

        when(accountUseCase.findByAccountNumber("001-100001-00")).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(account)).thenReturn(response);

        StepVerifier.create(accountController.findByAccountNumber("001-100001-00"))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(responseEntity.getBody()).isEqualTo(response);
                })
                .verifyComplete();

        verify(accountUseCase).findByAccountNumber("001-100001-00");
        verify(accountMapper).toResponse(account);
    }

    @Test
    void findByAccountNumber_shouldPropagateError_whenNotFound() {
        when(accountUseCase.findByAccountNumber("no-existe"))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        StepVerifier.create(accountController.findByAccountNumber("no-existe"))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountUseCase).findByAccountNumber("no-existe");
    }

    // ── DELETE /v1/cuentas/{accountNumber} ───────────────────────

    @Test
    void delete_shouldCallUseCaseAndReturnNoContent() {
        when(accountUseCase.deleteByAccountNumber("001-100001-00")).thenReturn(Mono.empty());

        StepVerifier.create(accountController.deleteByAccountNumber("001-100001-00"))
                .assertNext(responseEntity ->
                        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();

        verify(accountUseCase).deleteByAccountNumber("001-100001-00");
    }

    @Test
    void delete_shouldPropagateError_whenNotFound() {
        when(accountUseCase.deleteByAccountNumber("no-existe"))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        StepVerifier.create(accountController.deleteByAccountNumber("no-existe"))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    // ── PUT /v1/cuentas/{accountNumber} ──────────────────────────

    @Test
    void update_shouldCallUseCaseAndReturnOk() {
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountType("CORRIENTE")
                .balance(new BigDecimal("3000.00"))
                .status((short) 1)
                .build();

        Account account = AccountTestFactory.buildAccount();
        AccountResponseDTO response = AccountTestFactory.buildResponseDTO();

        when(accountMapper.updateToDomain(request)).thenReturn(account);
        when(accountUseCase.updateAccount("001-100001-00", account)).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(account)).thenReturn(response);

        StepVerifier.create(accountController.update("001-100001-00", request))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(responseEntity.getBody()).isEqualTo(response);
                })
                .verifyComplete();

        verify(accountMapper).updateToDomain(request);
        verify(accountUseCase).updateAccount("001-100001-00", account);
        verify(accountMapper).toResponse(account);
    }

    @Test
    void update_shouldPropagateError_whenNotFound() {
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountType("CORRIENTE")
                .balance(new BigDecimal("3000.00"))
                .status((short) 1)
                .build();

        Account account = AccountTestFactory.buildAccount();

        when(accountMapper.updateToDomain(request)).thenReturn(account);
        when(accountUseCase.updateAccount("no-existe", account))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        StepVerifier.create(accountController.update("no-existe", request))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    // ── PATCH /v1/cuentas/{accountNumber} ────────────────────────

    @Test
    void patch_shouldCallUseCaseAndReturnOk() {
        AccountPatchRequestDTO request = AccountPatchRequestDTO.builder()
                .status((short) 0)
                .build();

        Account account = AccountTestFactory.buildAccount();
        AccountResponseDTO response = AccountTestFactory.buildResponseDTO();

        when(accountUseCase.patchAccount("001-100001-00", request)).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(account)).thenReturn(response);

        StepVerifier.create(accountController.patch("001-100001-00", request))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(responseEntity.getBody()).isEqualTo(response);
                })
                .verifyComplete();

        verify(accountUseCase).patchAccount("001-100001-00", request);
        verify(accountMapper).toResponse(account);
    }

    @Test
    void patch_shouldPropagateError_whenNotFound() {
        AccountPatchRequestDTO request = AccountPatchRequestDTO.builder()
                .status((short) 0)
                .build();

        when(accountUseCase.patchAccount("no-existe", request))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        StepVerifier.create(accountController.patch("no-existe", request))
                .expectError(AccountNotFoundException.class)
                .verify();
    }
}