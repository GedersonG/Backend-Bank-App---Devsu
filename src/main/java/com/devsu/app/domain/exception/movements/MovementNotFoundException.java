package com.devsu.app.domain.exception.movements;

import com.devsu.app.domain.exception.BusinessException;

public class MovementNotFoundException extends BusinessException {
    public MovementNotFoundException(Long id) {
        super("MOVEMENT_NOT_FOUND", "Movimiento no encontrado con ID: " + id);
    }
}
