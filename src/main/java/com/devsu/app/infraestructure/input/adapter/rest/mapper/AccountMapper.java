package com.devsu.app.infraestructure.input.adapter.rest.mapper;

import com.devsu.app.domain.model.Account;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.account.AccountUpdateRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.account.AccountResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    AccountResponseDTO toResponse(Account account);

    Account toDomain(AccountRequestDTO request);

    Account updateToDomain(AccountUpdateRequestDTO request);

    default PageResponseDTO<AccountResponseDTO> toPageResponse(PageResponseDTO<Account> page) {
        return PageResponseDTO.<AccountResponseDTO>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getPage())
                .pageSize(page.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.isHasNext())
                .hasPrevious(page.isHasPrevious())
                .build();
    }
}
