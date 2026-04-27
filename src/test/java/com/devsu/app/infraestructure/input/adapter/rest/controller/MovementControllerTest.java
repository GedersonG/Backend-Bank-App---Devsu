package com.devsu.app.infraestructure.input.adapter.rest.controller;

import com.devsu.app.domain.exception.account.AccountNotFoundException;
import com.devsu.app.domain.exception.account.InactiveAccountException;
import com.devsu.app.domain.exception.movements.DailyLimitExceededException;
import com.devsu.app.domain.exception.movements.InsufficientFundsException;
import com.devsu.app.domain.exception.movements.InvalidReversalException;
import com.devsu.app.domain.exception.movements.MovementAlreadyReversedException;
import com.devsu.app.domain.exception.movements.MovementNotFoundException;
import com.devsu.app.domain.model.Movement;
import com.devsu.app.domain.port.in.MovementUseCase;
import com.devsu.app.infraestructure.input.adapter.rest.dto.request.movement.MovementRequestDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.PageResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.dto.response.movement.MovementResponseDTO;
import com.devsu.app.infraestructure.input.adapter.rest.mapper.MovementMapper;
import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;
import com.devsu.app.util.MovementTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementControllerTest {

    @Mock
    private MovementUseCase movementUseCase;

    @Mock
    private MovementMapper movementMapper;

    @InjectMocks
    private MovementController movementController;

    // ── POST /v1/movimientos ─────────────────────────────────────

    @Test
    void create_shouldCallUseCaseAndReturnCreated() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();
        MovementResponseDTO response = MovementTestFactory.buildResponseDTO();

        when(movementMapper.toDomain(request)).thenReturn(movement);
        when(movementUseCase.createMovement(movement)).thenReturn(Mono.just(movement));
        when(movementMapper.toResponse(movement)).thenReturn(response);

        StepVerifier.create(movementController.create(request))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();

        verify(movementMapper).toDomain(request);
        verify(movementUseCase).createMovement(movement);
        verify(movementMapper).toResponse(movement);
    }

    @Test
    void create_shouldPropagateError_whenAccountNotFound() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();

        when(movementMapper.toDomain(request)).thenReturn(movement);
        when(movementUseCase.createMovement(movement))
                .thenReturn(Mono.error(new AccountNotFoundException("001-100001-00")));

        StepVerifier.create(movementController.create(request))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(movementUseCase).createMovement(movement);
    }

    @Test
    void create_shouldPropagateError_whenInsufficientFunds() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();

        when(movementMapper.toDomain(request)).thenReturn(movement);
        when(movementUseCase.createMovement(movement))
                .thenReturn(Mono.error(new InsufficientFundsException("001-100001-00")));

        StepVerifier.create(movementController.create(request))
                .expectError(InsufficientFundsException.class)
                .verify();

    }

    @Test
    void create_shouldPropagateError_whenDailyLimitExceeded() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();

        when(movementMapper.toDomain(request)).thenReturn(movement);
        when(movementUseCase.createMovement(movement))
                .thenReturn(Mono.error(
                        new DailyLimitExceededException("001-100001-00", new BigDecimal("200.00"))));

        StepVerifier.create(movementController.create(request))
                .expectError(DailyLimitExceededException.class)
                .verify();

    }

    @Test
    void create_shouldPropagateError_whenAccountInactive() {
        MovementRequestDTO request = MovementTestFactory.buildRequestDTO();
        Movement movement = MovementTestFactory.buildMovement();

        when(movementMapper.toDomain(request)).thenReturn(movement);
        when(movementUseCase.createMovement(movement))
                .thenReturn(Mono.error(new InactiveAccountException("001-100001-00")));

        StepVerifier.create(movementController.create(request))
                .expectError(InactiveAccountException.class)
                .verify();

    }

    // ── GET /v1/movimientos ──────────────────────────────────────

    @Test
    void findAll_shouldCallUseCaseWithFiltersAndReturnOk() {
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 23, 59);

        PageResponseDTO<Movement> page = MovementTestFactory.buildMovementPage();
        PageResponseDTO<MovementResponseDTO> pageDTO = MovementTestFactory.buildMovementPageDTO();

        when(movementUseCase.findAll(any(MovementFilterDTO.class), eq(0), eq(20)))
                .thenReturn(Mono.just(page));
        when(movementMapper.toPageResponse(page)).thenReturn(pageDTO);

        StepVerifier.create(movementController.findAll(
                        0, 20, "001-100001-00", startDate, endDate))
                .assertNext(responseEntity ->
                        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(movementUseCase).findAll(any(MovementFilterDTO.class), eq(0), eq(20));
        verify(movementMapper).toPageResponse(page);
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoResults() {
        PageResponseDTO<Movement> emptyPage = PageResponseDTO.<Movement>builder()
                .content(List.of()).page(0).pageSize(0)
                .totalElements(0L).totalPages(0)
                .hasNext(false).hasPrevious(false).build();

        PageResponseDTO<MovementResponseDTO> emptyPageDTO = PageResponseDTO.<MovementResponseDTO>builder()
                .content(List.of()).page(0).pageSize(0)
                .totalElements(0L).totalPages(0)
                .hasNext(false).hasPrevious(false).build();

        when(movementUseCase.findAll(any(), eq(0), eq(20))).thenReturn(Mono.just(emptyPage));
        when(movementMapper.toPageResponse(emptyPage)).thenReturn(emptyPageDTO);

        StepVerifier.create(movementController.findAll(0, 20, null, null, null))
                .assertNext(responseEntity ->
                        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }

    // ── POST /v1/movimientos/{movementId}/reversar ───────────────

    @Test
    void reverse_shouldCallUseCaseAndReturnOk() {
        Movement movement = MovementTestFactory.buildMovement();
        MovementResponseDTO response = MovementTestFactory.buildResponseDTO();

        when(movementUseCase.reverseMovement(1L)).thenReturn(Mono.just(movement));
        when(movementMapper.toResponse(movement)).thenReturn(response);

        StepVerifier.create(movementController.reverse(1L))
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(responseEntity.getBody()).isEqualTo(response);
                })
                .verifyComplete();

        verify(movementUseCase).reverseMovement(1L);
        verify(movementMapper).toResponse(movement);
    }

    @Test
    void reverse_shouldPropagateError_whenMovementNotFound() {
        when(movementUseCase.reverseMovement(99L))
                .thenReturn(Mono.error(new MovementNotFoundException(99L)));

        StepVerifier.create(movementController.reverse(99L))
                .expectError(MovementNotFoundException.class)
                .verify();

        verify(movementUseCase).reverseMovement(99L);
    }

    @Test
    void reverse_shouldPropagateError_whenAlreadyReversed() {
        when(movementUseCase.reverseMovement(1L))
                .thenReturn(Mono.error(new MovementAlreadyReversedException(1L)));

        StepVerifier.create(movementController.reverse(1L))
                .expectError(MovementAlreadyReversedException.class)
                .verify();

    }

    @Test
    void reverse_shouldPropagateError_whenInvalidReversal() {
        when(movementUseCase.reverseMovement(1L))
                .thenReturn(Mono.error(new InvalidReversalException(1L)));

        StepVerifier.create(movementController.reverse(1L))
                .expectError(InvalidReversalException.class)
                .verify();

    }
}