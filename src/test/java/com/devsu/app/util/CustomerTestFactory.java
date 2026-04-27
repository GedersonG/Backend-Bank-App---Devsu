package com.devsu.app.util;

import com.devsu.app.domain.model.Customer;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerPatchRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.customer.CustomerResponseDTO;

import java.util.List;

public class CustomerTestFactory {

    public static Customer buildCustomer() {
        return Customer.builder()
                .id(1L)
                .clientId("uuid-test-123")
                .name("Gederson Guzman")
                .gender("Male")
                .age(25)
                .identification("1004808530")
                .address("Calle 1")
                .phone("3136303782")
                .status((short) 1)
                .build();
    }

    public static CustomerResponseDTO buildResponseDTO() {
        return CustomerResponseDTO.builder()
                .clientId("uuid-test-123")
                .name("Gederson Guzman")
                .identification("1004808530")
                .phone("3136303782")
                .address("Calle 1")
                .status((short) 1)
                .build();
    }

    public static CustomerRequestDTO buildRequestDTO() {
        return CustomerRequestDTO.builder()
                .name("Gederson Guzman")
                .identification("1004808530")
                .gender("Male")
                .age(25)
                .password("password123")
                .phone("3136303782")
                .address("Calle 1")
                .build();
    }

    public static CustomerUpdateRequestDTO buildUpdateRequestDTO() {
        return CustomerUpdateRequestDTO.builder()
                .name("Gederson Actualizado")
                .identification("1004808530")
                .gender("Male")
                .age(26)
                .phone("3136303782")
                .address("Calle 2")
                .build();
    }

    public static CustomerPatchRequestDTO buildPatchRequestDTO() {
        return CustomerPatchRequestDTO.builder()
                .phone("3001234567")
                .build();
    }

    public static PageResponseDTO<Customer> buildCustomerPage() {
        return PageResponseDTO.<Customer>builder()
                .content(List.of(buildCustomer()))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    public static PageResponseDTO<CustomerResponseDTO> buildCustomerPageDTO() {
        return PageResponseDTO.<CustomerResponseDTO>builder()
                .content(List.of(buildResponseDTO()))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}