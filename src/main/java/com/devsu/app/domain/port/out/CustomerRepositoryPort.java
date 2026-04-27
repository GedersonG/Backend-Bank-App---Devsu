package com.devsu.app.domain.port.out;

import com.devsu.app.domain.model.Customer;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import reactor.core.publisher.Mono;

public interface CustomerRepositoryPort {
    Mono<Boolean> existsByIdentification(String identification);
    Mono<Customer> findByClientId(String clientId);
    Mono<PaginatedResult<Customer>> findAll(CustomerFilterDTO filter, int pageSize, int offset);
    Mono<Customer> save(Customer customer);
    Mono<Customer> update(Customer customer);
    Mono<Void> deleteByClientId(String clientId);
}
