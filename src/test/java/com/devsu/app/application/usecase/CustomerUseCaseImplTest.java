package com.devsu.app.application.usecase;

import com.devsu.app.domain.exception.customer.CustomerNotFoundException;
import com.devsu.app.domain.exception.customer.DuplicateIdentificationException;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.port.out.CustomerRepositoryPort;

import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerPatchRequestDTO;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerUseCaseImpl customerUseCase;

    private Customer customer;
    private String clientId;
    private String identification;

    @BeforeEach
    void setUp() {
        clientId = "test-client-id";
        identification = "123456789";

        customer = Customer.builder()
                .clientId(clientId)
                .identification(identification)
                .name("John Doe")
                .password("rawPassword")
                .status((short) 1)
                .build();
    }

    @Test
    void findByClientId_ShouldReturnCustomer_WhenExists() {
        // Given
        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));

        // When & Then
        StepVerifier.create(customerUseCase.findByClientId(clientId))
                .expectNextMatches(found ->
                        found.getClientId().equals(clientId) &&
                                found.getName().equals("John Doe"))
                .verifyComplete();

        verify(customerRepository, times(1)).findByClientId(clientId);
    }

    @Test
    void findByClientId_ShouldThrowException_WhenNotFound() {
        // Given
        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(customerUseCase.findByClientId(clientId))
                .expectErrorMatches(throwable ->
                        throwable instanceof CustomerNotFoundException)
                .verify();

        verify(customerRepository, times(1)).findByClientId(clientId);
    }

    @Test
    void findAll_ShouldReturnPageResponseDTO_WhenCustomersExist() {
        // Given
        CustomerFilterDTO filter = new CustomerFilterDTO();
        int page = 0;
        int pageSize = 10;
        int offset = 0;

        List<Customer> customers = List.of(customer);
        long totalCount = 25L;

        PaginatedResult<Customer> PaginatedResult = new PaginatedResult<>(customers, totalCount);

        when(customerRepository.findAll(filter, pageSize, offset))
                .thenReturn(Mono.just(PaginatedResult));

        // When & Then
        StepVerifier.create(customerUseCase.findAll(filter, page, pageSize))
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().size() == 1 &&
                                pageResponse.getPage() == 0 &&
                                pageResponse.getPageSize() == 1 &&
                                pageResponse.getTotalElements() == 25L &&
                                pageResponse.getTotalPages() == 3 &&
                                pageResponse.isHasNext() &&
                                !pageResponse.isHasPrevious())
                .verifyComplete();

        verify(customerRepository, times(1)).findAll(filter, pageSize, offset);
    }

    @Test
    void findAll_ShouldCalculatePagesCorrectly_WhenExactDivision() {
        // Given
        CustomerFilterDTO filter = new CustomerFilterDTO();
        int page = 1;
        int pageSize = 10;
        int offset = 10;

        List<Customer> customers = List.of(customer);
        long totalCount = 20L;

        PaginatedResult<Customer> PaginatedResult = new PaginatedResult<>(customers, totalCount);

        when(customerRepository.findAll(filter, pageSize, offset))
                .thenReturn(Mono.just(PaginatedResult));

        // When & Then
        StepVerifier.create(customerUseCase.findAll(filter, page, pageSize))
                .expectNextMatches(pageResponse ->
                        pageResponse.getTotalPages() == 2 &&
                                !pageResponse.isHasNext() &&
                                pageResponse.isHasPrevious())
                .verifyComplete();
    }

    @Test
    void createCustomer_ShouldSaveCustomer_WhenIdentificationNotExists() {
        // Given
        when(customerRepository.existsByIdentification(identification))
                .thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(Mono.just(customer));

        // When & Then
        StepVerifier.create(customerUseCase.createCustomer(customer))
                .expectNextMatches(saved -> saved.getClientId() != null &&
                        saved.getStatus() == 1 &&
                        saved.getPassword().equals("encodedPassword"))
                .verifyComplete();

        verify(customerRepository, times(1)).existsByIdentification(identification);
        verify(passwordEncoder, times(1)).encode("rawPassword");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_ShouldThrowException_WhenIdentificationExists() {
        // Given
        when(customerRepository.existsByIdentification(identification))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(customerUseCase.createCustomer(customer))
                .expectError(DuplicateIdentificationException.class)
                .verify();

        verify(customerRepository, times(1)).existsByIdentification(identification);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_ShouldUpdateCustomer_WhenExists() {
        // Given
        Customer updatedCustomer = Customer.builder()
                .clientId(clientId)
                .name("Updated Name")
                .identification(identification)
                .password("newPassword")
                .build();

        Customer customerAfterApplyUpdate = customer.toBuilder()
                .name("Updated Name")
                .password("newPassword")  // Password sin encode aún
                .build();

        Customer customerAfterEncode = customerAfterApplyUpdate.toBuilder()
                .password("encodedNewPassword")
                .build();

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");
        when(customerRepository.update(any(Customer.class)))
                .thenReturn(Mono.just(customerAfterEncode));

        // When & Then
        StepVerifier.create(customerUseCase.updateCustomer(clientId, updatedCustomer))
                .expectNextMatches(updated ->
                        updated.getName().equals("Updated Name") &&
                                updated.getPassword().equals("encodedNewPassword"))
                .verifyComplete();

        verify(customerRepository, times(1)).findByClientId(clientId);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(customerRepository, times(1)).update(any(Customer.class));
    }

    @Test
    void updateCustomer_ShouldNotEncodePassword_WhenPasswordNull() {
        // Given
        Customer updatedCustomer = Customer.builder()
                .clientId(clientId)
                .name("Updated Name")
                .identification(identification)
                .password(null)
                .build();

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));
        when(customerRepository.update(any(Customer.class)))
                .thenReturn(Mono.just(customer));

        // When & Then
        StepVerifier.create(customerUseCase.updateCustomer(clientId, updatedCustomer))
                .expectNextCount(1)
                .verifyComplete();

        verify(customerRepository, times(1)).update(any(Customer.class));
    }

    @Test
    void updateCustomer_ShouldThrowException_WhenNotFound() {
        // Given
        Customer updatedCustomer = Customer.builder().build();

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(customerUseCase.updateCustomer(clientId, updatedCustomer))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository, times(1)).findByClientId(clientId);
        verify(customerRepository, never()).update(any(Customer.class));
    }

    @Test
    void patchCustomer_ShouldApplyPatch_WhenExists() {
        // Given
        CustomerPatchRequestDTO patch = CustomerPatchRequestDTO.builder()
                .name("Patched Name")
                .build();

        Customer patchedCustomer = Customer.builder()
                .clientId(clientId)
                .name("Patched Name")
                .identification(identification)
                .status((short) 1)
                .build();

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));
        when(customerRepository.update(any(Customer.class)))
                .thenReturn(Mono.just(patchedCustomer));

        // When & Then
        StepVerifier.create(customerUseCase.patchCustomer(clientId, patch))
                .expectNextMatches(patched ->
                        patched.getName().equals("Patched Name"))
                .verifyComplete();

        verify(customerRepository, times(1)).findByClientId(clientId);
        verify(customerRepository, times(1)).update(any(Customer.class));
    }

    @Test
    void patchCustomer_ShouldEncodePassword_WhenPatchContainsPassword() {
        // Given
        CustomerPatchRequestDTO patch = CustomerPatchRequestDTO.builder()
                .password("newPatchedPassword")
                .build();

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));
        when(passwordEncoder.encode("newPatchedPassword"))
                .thenReturn("encodedPatchedPassword");
        when(customerRepository.update(any(Customer.class)))
                .thenReturn(Mono.just(customer));

        // When & Then
        StepVerifier.create(customerUseCase.patchCustomer(clientId, patch))
                .expectNextCount(1)
                .verifyComplete();

        verify(passwordEncoder, times(1)).encode("newPatchedPassword");
        verify(customerRepository, times(1)).update(any(Customer.class));
    }

    @Test
    void patchCustomer_ShouldThrowException_WhenNotFound() {
        // Given
        CustomerPatchRequestDTO patch = new CustomerPatchRequestDTO();

        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(customerUseCase.patchCustomer(clientId, patch))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    void deleteCustomer_ShouldDelete_WhenExists() {
        // Given
        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.just(customer));
        when(customerRepository.deleteByClientId(clientId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(customerUseCase.deleteCustomer(clientId))
                .verifyComplete();

        verify(customerRepository, times(1)).findByClientId(clientId);
        verify(customerRepository, times(1)).deleteByClientId(clientId);
    }

    @Test
    void deleteCustomer_ShouldThrowException_WhenNotFound() {
        // Given
        when(customerRepository.findByClientId(clientId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(customerUseCase.deleteCustomer(clientId))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository, times(1)).findByClientId(clientId);
        verify(customerRepository, never()).deleteByClientId(anyString());
    }
}