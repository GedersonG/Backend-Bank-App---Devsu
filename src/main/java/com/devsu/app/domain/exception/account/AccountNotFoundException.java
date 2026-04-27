package com.devsu.app.domain.exception.account;

import com.devsu.app.domain.exception.BusinessException;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(String accountNumber) {
        super("ACCOUNT_NOT_FOUND", "Cuenta no encontrada con número: " + accountNumber);
    }
}
