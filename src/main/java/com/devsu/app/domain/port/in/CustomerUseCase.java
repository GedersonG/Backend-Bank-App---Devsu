package com.devsu.app.domain.port.in;

import com.devsu.app.domain.model.Customer;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;
import reactor.core.publisher.Mono;

public interface CustomerUseCase {
    Mono<Customer> findByClientId(String clientId);
    Mono<PageResponseDTO<Customer>> findAll(CustomerFilterDTO filter, int page, int pageSize);
    Mono<Customer> createCustomer(Customer customer);
    Mono<Customer> updateCustomer(String clientId, Customer customer);
    Mono<Customer> patchCustomer(String clientId, CustomerPatchRequestDTO patch);
    Mono<Void> deleteCustomer(String clientId);
}
