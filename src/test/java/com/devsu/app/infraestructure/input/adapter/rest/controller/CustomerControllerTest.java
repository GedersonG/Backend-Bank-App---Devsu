package com.devsu.app.infraestructure.input.adapter.rest.controller;

import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.exception.customer.DuplicateIdentificationException;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.port.in.CustomerUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.customer.CustomerResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.CustomerMapper;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;
import com.devsu.app.util.CustomerTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerUseCase customerUseCase;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerController customerController;

    // ── POST /v1/clientes ────────────────────────────────────────

    @Test
    void createCustomer_shouldCallUseCaseAndReturnCreated() {
        CustomerRequestDTO request = CustomerTestFactory.buildRequestDTO();
        Customer customer = CustomerTestFactory.buildCustomer();
        CustomerResponseDTO response = CustomerTestFactory.buildResponseDTO();

        when(customerMapper.toDomain(request)).thenReturn(customer);
        when(customerUseCase.createCustomer(customer)).thenReturn(Mono.just(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        StepVerifier.create(customerController.createCustomer(request))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();

        verify(customerMapper).toDomain(request);
        verify(customerUseCase).createCustomer(customer);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void createCustomer_shouldPropagateError_whenDuplicateIdentification() {
        CustomerRequestDTO request = CustomerTestFactory.buildRequestDTO();
        Customer customer = CustomerTestFactory.buildCustomer();

        when(customerMapper.toDomain(request)).thenReturn(customer);
        when(customerUseCase.createCustomer(customer))
                .thenReturn(Mono.error(new DuplicateIdentificationException("1004808530")));

        StepVerifier.create(customerController.createCustomer(request))
                .expectError(DuplicateIdentificationException.class)
                .verify();

        verify(customerUseCase).createCustomer(customer);
    }

    @Test
    void createCustomer_shouldPropagateError_whenUseCaseThrows() {
        CustomerRequestDTO request = CustomerTestFactory.buildRequestDTO();
        Customer customer = CustomerTestFactory.buildCustomer();

        when(customerMapper.toDomain(request)).thenReturn(customer);
        when(customerUseCase.createCustomer(customer))
                .thenReturn(Mono.error(new RuntimeException("Error inesperado")));

        StepVerifier.create(customerController.createCustomer(request))
                .expectError(RuntimeException.class)
                .verify();

    }

    // ── GET /v1/clientes ─────────────────────────────────────────

    @Test
    void findAllCustomers_shouldCallUseCaseWithFiltersAndReturnOk() {
        PageResponseDTO<Customer> page = CustomerTestFactory.buildCustomerPage();
        PageResponseDTO<CustomerResponseDTO> pageDTO = CustomerTestFactory.buildCustomerPageDTO();

        when(customerUseCase.findAll(any(CustomerFilterDTO.class), eq(0), eq(20)))
                .thenReturn(Mono.just(page));
        when(customerMapper.toPageResponse(page)).thenReturn(pageDTO);

        StepVerifier.create(customerController.findAllCustomers(0, 20, "1004808530", "Gederson"))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();

        verify(customerUseCase).findAll(any(CustomerFilterDTO.class), eq(0), eq(20));
        verify(customerMapper).toPageResponse(page);
    }

    @Test
    void findAllCustomers_shouldReturnEmptyPage_whenNoResults() {
        PageResponseDTO<Customer> emptyPage = PageResponseDTO.<Customer>builder()
                .content(List.of())
                .page(0).pageSize(0).totalElements(0L).totalPages(0)
                .hasNext(false).hasPrevious(false).build();

        PageResponseDTO<CustomerResponseDTO> emptyPageDTO = PageResponseDTO.<CustomerResponseDTO>builder()
                .content(List.of())
                .page(0).pageSize(0).totalElements(0L).totalPages(0)
                .hasNext(false).hasPrevious(false).build();

        when(customerUseCase.findAll(any(), eq(0), eq(20))).thenReturn(Mono.just(emptyPage));
        when(customerMapper.toPageResponse(emptyPage)).thenReturn(emptyPageDTO);

        StepVerifier.create(customerController.findAllCustomers(0, 20, null, null))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    // ── GET /v1/clientes/{clientId} ──────────────────────────────

    @Test
    void findByClientId_shouldCallUseCaseAndReturnOk() {
        Customer customer = CustomerTestFactory.buildCustomer();
        CustomerResponseDTO response = CustomerTestFactory.buildResponseDTO();

        when(customerUseCase.findByClientId("uuid-test-123")).thenReturn(Mono.just(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        StepVerifier.create(customerController.findByCustomerId("uuid-test-123"))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();

        verify(customerUseCase).findByClientId("uuid-test-123");
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void findByClientId_shouldPropagateError_whenNotFound() {
        when(customerUseCase.findByClientId("no-existe"))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        StepVerifier.create(customerController.findByCustomerId("no-existe"))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerUseCase).findByClientId("no-existe");
    }

    // ── PUT /v1/clientes/{clientId} ──────────────────────────────

    @Test
    void updateCustomer_shouldCallUseCaseAndReturnOk() {
        CustomerUpdateRequestDTO request = CustomerTestFactory.buildUpdateRequestDTO();
        Customer customer = CustomerTestFactory.buildCustomer();
        CustomerResponseDTO response = CustomerTestFactory.buildResponseDTO();

        when(customerMapper.updateToDomain(request)).thenReturn(customer);
        when(customerUseCase.updateCustomer("uuid-test-123", customer)).thenReturn(Mono.just(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        StepVerifier.create(customerController.updateCustomer("uuid-test-123", request))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(responseEntity.getBody()).isEqualTo(response);
                })
                .verifyComplete();

        verify(customerMapper).updateToDomain(request);
        verify(customerUseCase).updateCustomer("uuid-test-123", customer);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void updateCustomer_shouldPropagateError_whenNotFound() {
        CustomerUpdateRequestDTO request = CustomerTestFactory.buildUpdateRequestDTO();
        Customer customer = CustomerTestFactory.buildCustomer();

        when(customerMapper.updateToDomain(request)).thenReturn(customer);
        when(customerUseCase.updateCustomer("no-existe", customer))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        StepVerifier.create(customerController.updateCustomer("no-existe", request))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    void updateCustomer_shouldPropagateError_whenDuplicateIdentification() {
        CustomerUpdateRequestDTO request = CustomerTestFactory.buildUpdateRequestDTO();
        Customer customer = CustomerTestFactory.buildCustomer();

        when(customerMapper.updateToDomain(request)).thenReturn(customer);
        when(customerUseCase.updateCustomer("uuid-test-123", customer))
                .thenReturn(Mono.error(new DuplicateIdentificationException("1004808530")));

        StepVerifier.create(customerController.updateCustomer("uuid-test-123", request))
                .expectError(DuplicateIdentificationException.class)
                .verify();
    }

    // ── PATCH /v1/clientes/{clientId} ────────────────────────────

    @Test
    void patchCustomer_shouldCallUseCaseAndReturnOk() {
        CustomerPatchRequestDTO request = CustomerTestFactory.buildPatchRequestDTO();
        Customer customer = CustomerTestFactory.buildCustomer();
        CustomerResponseDTO response = CustomerTestFactory.buildResponseDTO();

        when(customerUseCase.patchCustomer("uuid-test-123", request)).thenReturn(Mono.just(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        StepVerifier.create(customerController.patchCustomer("uuid-test-123", request))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(responseEntity.getBody()).isEqualTo(response);
                })
                .verifyComplete();

        verify(customerUseCase).patchCustomer("uuid-test-123", request);
        verify(customerMapper).toResponse(customer);
    }


    // ── DELETE /v1/clientes/{clientId} ───────────────────────────

    @Test
    void inactiveCustomer_shouldCallUseCaseAndReturnNoContent() {
        when(customerUseCase.deleteCustomer("uuid-test-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerController.inactiveCustomer("uuid-test-123"))
                .assertNext(responseEntity ->
                        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();

        verify(customerUseCase).deleteCustomer("uuid-test-123");
    }

    @Test
    void inactiveCustomer_shouldPropagateError_whenNotFound() {
        when(customerUseCase.deleteCustomer("no-existe"))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        StepVerifier.create(customerController.inactiveCustomer("no-existe"))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerUseCase).deleteCustomer("no-existe");
    }
}