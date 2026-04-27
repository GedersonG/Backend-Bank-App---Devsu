package com.devsu.app.infraestructure.input.adapter.rest.mapper;

import com.devsu.app.domain.model.Movement;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.movement.MovementRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.movement.MovementResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovementMapper {

    MovementResponseDTO toResponse(Movement movement);

    Movement toDomain(MovementRequestDTO request);

    default PageResponseDTO<MovementResponseDTO> toPageResponse(PageResponseDTO<Movement> page) {
        return PageResponseDTO.<MovementResponseDTO>builder()
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
