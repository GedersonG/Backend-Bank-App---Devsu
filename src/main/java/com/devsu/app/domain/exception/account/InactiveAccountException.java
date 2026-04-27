package com.devsu.app.domain.exception.account;

import com.devsu.app.domain.exception.BusinessException;

public class InactiveAccountException extends BusinessException {
    public InactiveAccountException(String accountNumber) {
        super("INACTIVE_ACCOUNT", "La cuenta no está activa: " + accountNumber);
    }
}
