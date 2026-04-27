package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.exception.customer.DuplicateIdentificationException;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.port.in.CustomerUseCase;
import com.devsu.app.domain.port.out.CustomerRepositoryPort;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerUseCaseImpl implements CustomerUseCase {

    private final CustomerRepositoryPort customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Customer> findByClientId(String clientId) {
        return customerRepository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(
                        new CustomerNotFoundException("Customer not found: " + clientId)
                ));
    }

    @Override
    public Mono<PageResponseDTO<Customer>> findAll(CustomerFilterDTO filter, int page, int pageSize) {
        int offset = page * pageSize;

        return customerRepository.findAll(filter, pageSize, offset)
                .map(result -> {
                    long totalElements = result.totalCount();
                    int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                    return PageResponseDTO.<Customer>builder()
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
    public Mono<Customer> createCustomer(Customer customer) {
        return customerRepository.existsByIdentification(customer.getIdentification())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(
                                new DuplicateIdentificationException(customer.getIdentification())
                        );
                    }
                    customer.setClientId(UUID.randomUUID().toString());
                    customer.setStatus((short) 1);
                    customer.setPassword(passwordEncoder.encode(customer.getPassword()));
                    return customerRepository.save(customer);
                });
    }

    @Override
    @Transactional
    public Mono<Customer> updateCustomer(String clientId, Customer incoming) {
        return customerRepository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(clientId)))
                .map(existing -> existing.applyUpdate(incoming))
                .map(this::encodePasswordIfPresent)
                .flatMap(customerRepository::update);
    }

    @Override
    @Transactional
    public Mono<Customer> patchCustomer(String clientId, CustomerPatchRequestDTO patch) {
        return customerRepository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(clientId)))
                .map(existing -> existing.applyPatch(patch))
                .map(this::encodePasswordIfPresent)
                .flatMap(customerRepository::update);
    }

    @Override
    @Transactional
    public Mono<Void> deleteCustomer(String clientId) {
        return customerRepository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(clientId)))
                .flatMap(existing -> customerRepository.deleteByClientId(clientId));
    }

    private Customer encodePasswordIfPresent(Customer customer) {
        if (customer.getPassword() != null) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        }
        return customer;
    }
}
