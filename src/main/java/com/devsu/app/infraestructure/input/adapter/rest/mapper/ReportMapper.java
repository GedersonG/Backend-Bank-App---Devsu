package com.devsu.app.infraestructure.input.adapter.rest.mapper;

import com.devsu.app.domain.model.AccountStatement;
import com.devsu.app.domain.model.CustomerStatement;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.report.AccountStatementDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.report.CustomerStatementResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MovementMapper.class}
)
public interface ReportMapper {

    AccountStatementDTO toAccountStatementDTO(AccountStatement account);

    CustomerStatementResponseDTO toResponse(CustomerStatement statement);
}
