package com.devsu.app.domain.exception.customer;

import com.devsu.app.domain.exception.BusinessException;

public class CustomerNotFoundException extends BusinessException {

    public CustomerNotFoundException(String clientId) {
        super("CUSTOMER_NOT_FOUND", "Cliente no encontrado con ID: " + clientId);
    }
}
