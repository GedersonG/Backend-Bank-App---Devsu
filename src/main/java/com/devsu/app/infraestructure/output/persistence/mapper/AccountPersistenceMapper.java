package com.devsu.app.infraestructure.output.persistence.mapper;

import com.devsu.app.domain.model.Account;
import com.devsu.app.infraestructure.output.persistence.dto.AccountInsertProperties;
import com.devsu.app.infraestructure.output.persistence.entity.AccountView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountPersistenceMapper {

    Account accountViewtoAccount(AccountView view);

    @Mapping(target = "createdBy", constant = "BACKEND_APP")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    AccountInsertProperties toInsertProperties(Account account);
}
