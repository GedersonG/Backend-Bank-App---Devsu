package com.devsu.app.integration;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.account.DuplicateAccountNumberException;
import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.model.Account;
import com.devsu.app.domain.port.in.AccountUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.controller.AccountController;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.account.AccountResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.AccountMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static com.devsu.app.util.AccountTestFactory.buildAccount;
import static com.devsu.app.util.AccountTestFactory.buildResponseDTO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(AccountController.class)
@ExtendWith(MockitoExtension.class)
class AccountIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountUseCase accountUseCase;

    @MockitoBean
    private AccountMapper accountMapper;

    // ── POST /v1/cuentas ─────────────────────────────────────────

    @Test
    void createAccount_shouldReturn201_whenValidRequest() {
        AccountRequestDTO request = AccountRequestDTO.builder()
                .accountType("Ahorro")
                .balance(new BigDecimal("5000.00"))
                .clientId("uuid-test-123")
                .build();

        Account account = buildAccount();
        AccountResponseDTO response = buildResponseDTO();

        when(accountMapper.toDomain(any(AccountRequestDTO.class))).thenReturn(account);
        when(accountUseCase.createAccount(any(Account.class))).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(any(Account.class))).thenReturn(response);

        webTestClient.post()
                .uri("/v1/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void createAccount_shouldReturn400_whenAccountTypeIsInvalid() {
        AccountRequestDTO request = AccountRequestDTO.builder()
                .accountType("INVALIDO")
                .balance(new BigDecimal("5000.00"))
                .clientId("uuid-test-123")
                .build();

        webTestClient.post()
                .uri("/v1/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createAccount_shouldReturn404_whenCustomerNotFound() {
        AccountRequestDTO request = AccountRequestDTO.builder()
                .accountType("Ahorro")
                .balance(new BigDecimal("5000.00"))
                .clientId("no-existe")
                .build();

        Account account = buildAccount();

        when(accountMapper.toDomain(any())).thenReturn(account);
        when(accountUseCase.createAccount(any()))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        webTestClient.post()
                .uri("/v1/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void createAccount_shouldReturn409_whenDuplicateAccountNumber() {
        AccountRequestDTO request = AccountRequestDTO.builder()
                .accountType("Ahorro")
                .balance(new BigDecimal("5000.00"))
                .clientId("uuid-test-123")
                .build();

        Account account = buildAccount();

        when(accountMapper.toDomain(any())).thenReturn(account);
        when(accountUseCase.createAccount(any()))
                .thenReturn(Mono.error(new DuplicateAccountNumberException("001-100001-00")));

        webTestClient.post()
                .uri("/v1/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("DUPLICATE_ACCOUNT_NUMBER");
    }

    // ── GET /v1/cuentas ──────────────────────────────────────────

    @Test
    void getAllAccounts_shouldReturn200_withPaginatedResult() {
        Account account = buildAccount();
        AccountResponseDTO response = buildResponseDTO();

        PageResponseDTO<Account> page = PageResponseDTO.<Account>builder()
                .content(List.of(account))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponseDTO<AccountResponseDTO> pageDTO = PageResponseDTO.<AccountResponseDTO>builder()
                .content(List.of(response))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(accountUseCase.findAll(any(), eq(0), eq(20))).thenReturn(Mono.just(page));
        when(accountMapper.toPageResponse(any())).thenReturn(pageDTO);

        webTestClient.get()
                .uri("/v1/cuentas?page=0&pageSize=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.content[0].accountNumber").isEqualTo("001-100001-00");
    }

    @Test
    void getAllAccounts_shouldReturn200_withFilters() {
        PageResponseDTO<AccountResponseDTO> pageDTO = PageResponseDTO.<AccountResponseDTO>builder()
                .content(List.of())
                .page(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(accountUseCase.findAll(any(), eq(0), eq(20)))
                .thenReturn(Mono.just(PageResponseDTO.<Account>builder()
                        .content(List.of()).page(0).pageSize(0)
                        .totalElements(0L).totalPages(0)
                        .hasNext(false).hasPrevious(false).build()));
        when(accountMapper.toPageResponse(any())).thenReturn(pageDTO);

        webTestClient.get()
                .uri("/v1/cuentas?page=0&pageSize=20&accountType=Ahorro&customerIdentification=1004")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(0);
    }

    // ── GET /v1/cuentas/{accountNumber} ──────────────────────────

    @Test
    void findByAccountNumber_shouldReturn200_whenAccountExists() {
        Account account = buildAccount();
        AccountResponseDTO response = buildResponseDTO();

        when(accountUseCase.findByAccountNumber("001-100001-00")).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(any())).thenReturn(response);

        webTestClient.get()
                .uri("/v1/cuentas/001-100001-00")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void findByAccountNumber_shouldReturn404_whenNotFound() {
        when(accountUseCase.findByAccountNumber("no-existe"))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        webTestClient.get()
                .uri("/v1/cuentas/no-existe")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("ACCOUNT_NOT_FOUND");
    }

    // ── DELETE /v1/cuentas/{accountNumber} ───────────────────────

    @Test
    void delete_shouldReturn204_whenAccountExists() {
        when(accountUseCase.deleteByAccountNumber("001-100001-00")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/v1/cuentas/001-100001-00")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void delete_shouldReturn404_whenNotFound() {
        when(accountUseCase.deleteByAccountNumber("no-existe"))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        webTestClient.delete()
                .uri("/v1/cuentas/no-existe")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── PUT /v1/cuentas/{accountNumber} ──────────────────────────

    @Test
    void update_shouldReturn200_whenValidRequest() {
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountType("Ahorro")
                .balance(new BigDecimal("3000.00"))
                .status((short) 1)
                .build();

        Account account = buildAccount();
        AccountResponseDTO response = buildResponseDTO();

        when(accountMapper.updateToDomain(any())).thenReturn(account);
        when(accountUseCase.updateAccount(eq("001-100001-00"), any())).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(any())).thenReturn(response);

        webTestClient.put()
                .uri("/v1/cuentas/001-100001-00")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void update_shouldReturn404_whenNotFound() {
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountType("Ahorro")
                .balance(new BigDecimal("3000.00"))
                .status((short) 1)
                .build();

        Account account = buildAccount();

        when(accountMapper.updateToDomain(any())).thenReturn(account);
        when(accountUseCase.updateAccount(eq("no-existe"), any()))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        webTestClient.put()
                .uri("/v1/cuentas/no-existe")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── PATCH /v1/cuentas/{accountNumber} ────────────────────────

    @Test
    void patch_shouldReturn200_whenValidRequest() {
        AccountPatchRequestDTO request = AccountPatchRequestDTO.builder()
                .status((short) 0)
                .build();

        Account account = buildAccount();
        AccountResponseDTO response = buildResponseDTO();

        when(accountUseCase.patchAccount(eq("001-100001-00"), any())).thenReturn(Mono.just(account));
        when(accountMapper.toResponse(any())).thenReturn(response);

        webTestClient.patch()
                .uri("/v1/cuentas/001-100001-00")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void patch_shouldReturn404_whenNotFound() {
        AccountPatchRequestDTO request = AccountPatchRequestDTO.builder()
                .status((short) 0)
                .build();

        when(accountUseCase.patchAccount(eq("no-existe"), any()))
                .thenReturn(Mono.error(new AccountNotFoundException("no-existe")));

        webTestClient.patch()
                .uri("/v1/cuentas/no-existe")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }
}