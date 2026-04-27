package com.devsu.app.domain.exception.customer;

import com.devsu.app.domain.exception.BusinessException;

public class DuplicateClientIdException extends BusinessException {

    public DuplicateClientIdException(String clientId) {
        super("DUPLICATE_CLIENT_ID", "Ya existe un cliente con ese ID: " + clientId);
    }
}
