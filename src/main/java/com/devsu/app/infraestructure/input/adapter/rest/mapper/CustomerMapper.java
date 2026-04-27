package com.devsu.app.infraestructure.input.adapter.rest.mapper;

import com.devsu.app.domain.model.Customer;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.customer.CustomerResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {

    CustomerResponseDTO toResponse(Customer customer);

    Customer toDomain(CustomerRequestDTO request);

    Customer updateToDomain(CustomerUpdateRequestDTO request);

    default PageResponseDTO<CustomerResponseDTO> toPageResponse(PageResponseDTO<Customer> page) {
        return PageResponseDTO.<CustomerResponseDTO>builder()
                .content(page.getContent().stream()
                        .map(this::toResponse)
                        .toList())
                .page(page.getPage())
                .pageSize(page.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.isHasNext())
                .hasPrevious(page.isHasPrevious())
                .build();
    }
}
