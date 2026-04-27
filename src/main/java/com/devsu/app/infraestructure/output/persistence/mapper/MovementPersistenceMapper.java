package com.devsu.app.infraestructure.output.persistence.mapper;

import com.devsu.app.domain.model.Movement;
import com.devsu.app.infraestructure.output.persistence.dto.MovementInsertProperties;
import com.devsu.app.infraestructure.output.persistence.entity.MovementView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovementPersistenceMapper {

    Movement movementViewToMovement(MovementView view);

    @Mapping(target = "createdBy", constant = "BACKEND_APP")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "movementDate", expression = "java(java.time.LocalDateTime.now())")
    MovementInsertProperties toInsertProperties(Movement movement);

    default Movement rowToMovement(Map<String, Object> row) {
        return Movement.builder()
                .id((Long) row.get("id"))
                .movementDate((LocalDateTime) row.get("movement_date"))
                .movementType((String) row.get("movement_type"))
                .value((BigDecimal) row.get("value"))
                .balance((BigDecimal) row.get("balance"))
                .accountId((Long) row.get("account_id"))
                .accountNumber((String) row.get("account_number"))
                .accountType((String) row.get("account_type"))
                .customerName((String) row.get("customer_name"))
                .customerIdentification((String) row.get("customer_identification"))
                .clientId((String) row.get("client_id"))
                .referenceAccountId(row.get("reference_account_id") != null
                        ? ((Number) row.get("reference_account_id")).longValue() : null)
                .referenceMovementId(row.get("reference_movement_id") != null
                        ? ((Number) row.get("reference_movement_id")).longValue() : null)
                .referenceAccountNumber((String) row.get("reference_account_number"))
                .referenceCustomerName((String) row.get("reference_customer_name"))
                .createdAt((LocalDateTime) row.get("created_at"))
                .updatedAt((LocalDateTime) row.get("updated_at"))
                .build();
    }
}
