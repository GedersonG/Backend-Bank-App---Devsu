package com.devsu.app.domain.port.in;

import com.devsu.app.domain.model.Movement;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import reactor.core.publisher.Mono;

public interface MovementUseCase {
    Mono<Movement> createMovement(Movement movement);
    Mono<PageResponseDTO<Movement>> findAll(MovementFilterDTO filter, int page, int pageSize);
    Mono<Movement> reverseMovement(Long movementId);
}
