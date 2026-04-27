package com.devsu.app.integration;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.movements.DailyLimitExceededException;
import com.devsu.app.domain.exception.movements.InsufficientFundsException;
import com.devsu.app.domain.exception.movements.InvalidReversalException;
import com.devsu.app.domain.exception.movements.MovementAlreadyReversedException;
import com.devsu.app.domain.exception.movements.MovementNotFoundException;
import com.devsu.app.domain.model.Movement;
import com.devsu.app.domain.port.in.MovementUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.controller.MovementController;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.movement.MovementRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.movement.MovementResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.MovementMapper;
import com.devsu.app.util.MovementTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(MovementController.class)
@ExtendWith(MockitoExtension.class)
class MovementsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MovementUseCase movementUseCase;

    @MockitoBean
    private MovementMapper movementMapper;

    // ── POST /v1/movimientos ─────────────────────────────────────

    @Test
    void create_shouldReturn201_whenValidRequest() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();
        MovementResponseDTO response = MovementTestFactory.buildResponseDTO();

        when(movementMapper.toDomain(any())).thenReturn(movement);
        when(movementUseCase.createMovement(any())).thenReturn(Mono.just(movement));
        when(movementMapper.toResponse(any())).thenReturn(response);

        webTestClient.post()
                .uri("/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.movementType").isEqualTo("Credito")
                .jsonPath("$.value").isEqualTo(500.00)
                .jsonPath("$.accountNumber").isEqualTo("001-100001-00");
    }

    @Test
    void create_shouldReturn400_whenAccountNumberIsBlank() {
        MovementRequestDTO request = MovementRequestDTO.builder()
                .accountNumber("")
                .movementType("Credito")
                .value(new BigDecimal("500.00"))
                .build();

        webTestClient.post()
                .uri("/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void create_shouldReturn400_whenValueIsZero() {
        MovementRequestDTO request = MovementRequestDTO.builder()
                .accountNumber("001-100001-00")
                .movementType("Credito")
                .value(BigDecimal.ZERO)
                .build();

        webTestClient.post()
                .uri("/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void create_shouldReturn404_whenAccountNotFound() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();

        when(movementMapper.toDomain(any())).thenReturn(movement);
        when(movementUseCase.createMovement(any()))
                .thenReturn(Mono.error(new AccountNotFoundException("001-100001-00")));

        webTestClient.post()
                .uri("/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("ACCOUNT_NOT_FOUND");
    }

    @Test
    void create_shouldReturn422_whenInsufficientFunds() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();

        when(movementMapper.toDomain(any())).thenReturn(movement);
        when(movementUseCase.createMovement(any()))
                .thenReturn(Mono.error(new InsufficientFundsException("001-100001-00")));

        webTestClient.post()
                .uri("/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INSUFFICIENT_FUNDS");
    }

    @Test
    void create_shouldReturn422_whenDailyLimitExceeded() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();

        when(movementMapper.toDomain(any())).thenReturn(movement);
        when(movementUseCase.createMovement(any()))
                .thenReturn(Mono.error(
                        new DailyLimitExceededException("001-100001-00", new BigDecimal("200.00"))));

        webTestClient.post()
                .uri("/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("DAILY_LIMIT_EXCEEDED");
    }

    // ── GET /v1/movimientos ──────────────────────────────────────

    @Test
    void findAll_shouldReturn200_withFilters() {
        PageResponseDTO<MovementResponseDTO> pageDTO = MovementTestFactory.buildMovementPageDTO();

        when(movementUseCase.findAll(any(), eq(0), eq(20)))
                .thenReturn(Mono.just(MovementTestFactory.buildMovementPage()));
        when(movementMapper.toPageResponse(any())).thenReturn(pageDTO);

        webTestClient.get()
                .uri("/v1/movimientos?page=0&pageSize=20&accountNumber=001-100001-00")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.content[0].movementType").isEqualTo("Credito");
    }

    // ── POST /v1/movimientos/{movementId}/reversar ───────────────

    @Test
    void reverse_shouldReturn200_whenValid() {
        Movement movement = MovementTestFactory.buildMovement();
        MovementResponseDTO response = MovementTestFactory.buildResponseDTO();

        when(movementUseCase.reverseMovement(1L)).thenReturn(Mono.just(movement));
        when(movementMapper.toResponse(any())).thenReturn(response);

        webTestClient.post()
                .uri("/v1/movimientos/1/reversar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.movementType").isEqualTo("Credito");
    }

    @Test
    void reverse_shouldReturn404_whenMovementNotFound() {
        when(movementUseCase.reverseMovement(99L))
                .thenReturn(Mono.error(new MovementNotFoundException(99L)));

        webTestClient.post()
                .uri("/v1/movimientos/99/reversar")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("MOVEMENT_NOT_FOUND");
    }

    @Test
    void reverse_shouldReturn409_whenAlreadyReversed() {
        when(movementUseCase.reverseMovement(1L))
                .thenReturn(Mono.error(new MovementAlreadyReversedException(1L)));

        webTestClient.post()
                .uri("/v1/movimientos/1/reversar")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("MOVEMENT_ALREADY_REVERSED");
    }

    @Test
    void reverse_shouldReturn422_whenInvalidReversal() {
        when(movementUseCase.reverseMovement(1L))
                .thenReturn(Mono.error(new InvalidReversalException(1L)));

        webTestClient.post()
                .uri("/v1/movimientos/1/reversar")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INVALID_REVERSAL");
    }
}
