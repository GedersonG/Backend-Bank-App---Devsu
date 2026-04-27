package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.model.Account;
import com.devsu.app.domain.port.in.AccountUseCase;
import com.devsu.app.domain.port.out.AccountRepositoryPort;
import com.devsu.app.domain.port.out.CustomerRepositoryPort;
import com.devsu.app.domain.service.AccountNumberGenerator;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountUseCaseImpl implements AccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final CustomerRepositoryPort customerRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Value("${banking.account.default-daily-limit}")
    private BigDecimal defaultDailyLimit;

    @Override
    public Mono<PageResponseDTO<Account>> findAll(AccountFilterDTO filter, int page, int pageSize) {
        int offset = page * pageSize;

        return accountRepository.findAll(filter, pageSize, offset)
                .map(result -> {
                    long totalElements = result.totalCount();
                    int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                    return PageResponseDTO.<Account>builder()
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
    public Mono<Account> createAccount(Account account) {
        return customerRepository.findByClientId(account.getClientId())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(account.getClientId())))
                .flatMap(customer -> {
                    account.setCustomerId(customer.getId());
                    account.setAccountNumber(accountNumberGenerator.generate(account.getAccountType()));
                    account.setDailyLimit(defaultDailyLimit);
                    account.setStatus((short) 1);
                    account.setBalance(account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO);
                    return accountRepository.save(account);
                });
    }

    @Override
    public Mono<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(
                        new AccountNotFoundException(accountNumber)
                ));
    }

    @Override
    public Mono<Void> deleteByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(
                        new AccountNotFoundException(accountNumber)
                ))
                .flatMap(existing -> accountRepository.deleteByAccountNumber(accountNumber));
    }

    @Override
    @Transactional
    public Mono<Account> updateAccount(String accountNumber, Account incoming) {
        return accountRepository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(accountNumber)))
                .map(existing -> existing.applyUpdate(incoming))
                .flatMap(accountRepository::update);
    }

    @Override
    @Transactional
    public Mono<Account> patchAccount(String accountNumber, AccountPatchRequestDTO patch) {
        return accountRepository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(accountNumber)))
                .map(existing -> existing.applyPatch(patch))
                .flatMap(accountRepository::update);
    }

    @Override
    public Mono<Long> resetDailyLimits() {
        return accountRepository.resetDailyLimits(defaultDailyLimit);
    }
}
