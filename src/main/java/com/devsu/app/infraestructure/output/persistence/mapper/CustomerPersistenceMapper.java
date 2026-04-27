package com.devsu.app.infraestructure.output.persistence.mapper;

import com.devsu.app.domain.model.Customer;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerInsertProperties;
import com.devsu.app.infraestructure.output.persistence.entity.CustomerView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerPersistenceMapper {

    Customer customerViewtoCustomer(CustomerView view);

    @Mapping(target = "createdBy", constant = "BACKEND_APP")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    CustomerInsertProperties toInsertProperties(Customer customer);

    default Customer rowToDomain(Map<String, Object> row) {
        return Customer.builder()
                .id((Long) row.get("id"))
                .name((String) row.get("name"))
                .gender((String) row.get("gender"))
                .age((Integer) row.get("age"))
                .identification((String) row.get("identification"))
                .address((String) row.get("address"))
                .phone((String) row.get("phone"))
                .createdBy((String) row.get("created_by"))
                .updatedBy((String) row.get("updated_by"))
                .createdAt((LocalDateTime) row.get("created_at"))
                .updatedAt((LocalDateTime) row.get("updated_at"))
                .clientId((String) row.get("client_id"))
                .password((String) row.get("password"))
                .status(((Short) row.get("status")))
                .build();
    }
}
