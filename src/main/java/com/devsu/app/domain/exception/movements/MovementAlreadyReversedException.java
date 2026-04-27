package com.devsu.app.domain.exception.movements;

import com.devsu.app.domain.exception.BusinessException;

public class MovementAlreadyReversedException extends BusinessException {
    public MovementAlreadyReversedException(Long id) {
        super("MOVEMENT_ALREADY_REVERSED", "El movimiento ya fue reversado: " + id);
    }
}