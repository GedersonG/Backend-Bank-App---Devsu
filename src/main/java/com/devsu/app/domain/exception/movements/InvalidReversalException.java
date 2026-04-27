package com.devsu.app.domain.exception.movements;

import com.devsu.app.domain.exception.BusinessException;

public class InvalidReversalException extends BusinessException {
    public InvalidReversalException(Long movementId) {
        super("INVALID_REVERSAL",
                "Solo se puede reversar desde la cuenta receptora. Movimiento ID: " + movementId);
    }
}
