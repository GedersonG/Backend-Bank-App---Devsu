package com.devsu.app.domain.exception.customer;

import com.devsu.app.domain.exception.BusinessException;

public class DuplicateIdentificationException extends BusinessException {

    public DuplicateIdentificationException(String identification) {
        super("DUPLICATE_IDENTIFICATION", "Ya existe un cliente con identificación: " + identification);
    }
}
