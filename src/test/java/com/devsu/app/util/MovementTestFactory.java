package com.devsu.app.util;

import com.devsu.app.domain.model.Movement;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.movement.MovementRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.movement.MovementResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MovementTestFactory {

    public static Movement buildMovement() {
        return Movement.builder()
                .id(1L)
                .movementDate(LocalDateTime.of(2026, 4, 26, 10, 0))
                .movementType("Credito")
                .value(new BigDecimal("500.00"))
                .balance(new BigDecimal("1500.00"))
                .accountId(1L)
                .accountNumber("001-100001-00")
                .accountType("AHORRO")
                .clientId("uuid-test-123")
                .customerName("Gederson Guzman")
                .customerIdentification("1004808530")
                .build();
    }

    public static MovementRequestDTO buildRequestDTO() {
        return MovementRequestDTO.builder()
                .accountNumber("001-100001-00")
                .movementType("Credito")
                .value(new BigDecimal("500.00"))
                .build();
    }

    public static MovementResponseDTO buildResponseDTO() {
        return MovementResponseDTO.builder()
                .id(1L)
                .movementDate(LocalDateTime.of(2026, 4, 26, 10, 0))
                .movementType("Credito")
                .value(new BigDecimal("500.00"))
                .balance(new BigDecimal("1500.00"))
                .accountNumber("001-100001-00")
                .accountType("AHORRO")
                .clientId("uuid-test-123")
                .customerName("Gederson Guzman")
                .build();
    }

    public static PageResponseDTO<Movement> buildMovementPage() {
        return PageResponseDTO.<Movement>builder()
                .content(List.of(buildMovement()))
                .page(0)
                .pageSize(1)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    public static PageResponseDTO<MovementResponseDTO> buildMovementPageDTO() {
        return PageResponseDTO.<MovementResponseDTO>builder()
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
