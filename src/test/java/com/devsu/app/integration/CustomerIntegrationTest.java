package com.devsu.app.integration;

import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.exception.customer.DuplicateIdentificationException;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.port.in.CustomerUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.controller.CustomerController;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.customer.CustomerResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.CustomerMapper;
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

import java.util.List;

import static com.devsu.app.util.CustomerTestFactory.buildCustomer;
import static com.devsu.app.util.CustomerTestFactory.buildResponseDTO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(CustomerController.class)
@ExtendWith(MockitoExtension.class)
class CustomerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerUseCase customerUseCase;

    @MockitoBean
    private CustomerMapper customerMapper;

    @Test
    void create_shouldReturn201_whenValidRequest() {
        CustomerRequestDTO request = CustomerRequestDTO.builder()
                .name("Gederson Guzman")
                .identification("1004808530")
                .gender("Masculino")
                .age(25)
                .password("password123")
                .phone("3136303782")
                .address("Calle 1")
                .build();

        Customer customer = buildCustomer();
        CustomerResponseDTO response = buildResponseDTO();

        when(customerMapper.toDomain(any(CustomerRequestDTO.class))).thenReturn(customer);
        when(customerUseCase.createCustomer(any(Customer.class))).thenReturn(Mono.just(customer));
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(response);

        webTestClient.post()
                .uri("/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseDTO.class)
                .value(dto -> {
                    assert dto != null;
                    assertThat(dto.getClientId()).isEqualTo("uuid-test-123");
                    assertThat(dto.getName()).isEqualTo("Gederson Guzman");
                });
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() {
        CustomerRequestDTO request = CustomerRequestDTO.builder()
                .name("")
                .identification("1004808530")
                .age(25)
                .password("password123")
                .phone("3136303782")
                .address("Calle 1")
                .build();

        webTestClient.post()
                .uri("/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void create_shouldReturn409_whenDuplicateIdentification() {
        CustomerRequestDTO request = CustomerRequestDTO.builder()
                .name("Gederson Guzman")
                .identification("1004808530")
                .gender("Masculino")
                .age(25)
                .password("password123")
                .phone("3136303782")
                .address("Calle 1")
                .build();

        Customer customer = buildCustomer();

        when(customerMapper.toDomain(any())).thenReturn(customer);
        when(customerUseCase.createCustomer(any()))
                .thenReturn(Mono.error(new DuplicateIdentificationException("1004808530")));

        webTestClient.post()
                .uri("/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("DUPLICATE_IDENTIFICATION");
    }

    // ── GET /v1/clientes/{clientId} ──────────────────────────────

    @Test
    void findByClientId_shouldReturn200_whenCustomerExists() {
        Customer customer = buildCustomer();
        CustomerResponseDTO response = buildResponseDTO();

        when(customerUseCase.findByClientId("uuid-test-123")).thenReturn(Mono.just(customer));
        when(customerMapper.toResponse(any())).thenReturn(response);

        webTestClient.get()
                .uri("/v1/clientes/uuid-test-123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDTO.class)
                .value(dto -> {
                    assert dto != null;
                    assertThat(dto.getClientId()).isEqualTo("uuid-test-123");
                });
    }

    @Test
    void findByClientId_shouldReturn404_whenCustomerNotFound() {
        when(customerUseCase.findByClientId("no-existe"))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        webTestClient.get()
                .uri("/v1/clientes/no-existe")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("CUSTOMER_NOT_FOUND");
    }

    // ── GET /v1/clientes ─────────────────────────────────────────

    @Test
    void findAll_shouldReturn200_withPaginatedResult() {
        Customer customer = buildCustomer();
        CustomerResponseDTO response = buildResponseDTO();

        PageResponseDTO<Customer> page = PageResponseDTO.<Customer>builder()
                .content(List.of(customer))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponseDTO<CustomerResponseDTO> pageDTO = PageResponseDTO.<CustomerResponseDTO>builder()
                .content(List.of(response))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(customerUseCase.findAll(any(), eq(0), eq(20))).thenReturn(Mono.just(page));
        when(customerMapper.toPageResponse(any())).thenReturn(pageDTO);

        webTestClient.get()
                .uri("/v1/clientes?page=0&pageSize=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.content[0].clientId").isEqualTo("uuid-test-123");
    }

    // ── DELETE /v1/clientes/{clientId} ───────────────────────────

    @Test
    void delete_shouldReturn204_whenCustomerExists() {
        when(customerUseCase.deleteCustomer("uuid-test-123")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/v1/clientes/uuid-test-123")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void delete_shouldReturn404_whenCustomerNotFound() {
        when(customerUseCase.deleteCustomer("no-existe"))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        webTestClient.delete()
                .uri("/v1/clientes/no-existe")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── PUT /v1/clientes/{clientId} ──────────────────────────────

    @Test
    void update_shouldReturn200_whenValidRequest() {
        CustomerUpdateRequestDTO request = CustomerUpdateRequestDTO.builder()
                .name("Gederson Actualizado")
                .identification("1004808530")
                .age(26)
                .phone("3136303782")
                .address("Calle 2")
                .build();

        Customer customer = buildCustomer();
        CustomerResponseDTO response = buildResponseDTO();

        when(customerMapper.updateToDomain(any())).thenReturn(customer);
        when(customerUseCase.updateCustomer(eq("uuid-test-123"), any())).thenReturn(Mono.just(customer));
        when(customerMapper.toResponse(any())).thenReturn(response);

        webTestClient.put()
                .uri("/v1/clientes/uuid-test-123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
}