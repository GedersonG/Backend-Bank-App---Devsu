package com.devsu.app.domain.exception.account;

import com.devsu.app.domain.exception.BusinessException;

public class DuplicateAccountNumberException extends BusinessException {

    public DuplicateAccountNumberException(String accountNumber) {
        super("DUPLICATE_ACCOUNT_NUMBER", "Ya existe una cuenta con el número: " + accountNumber);
    }
}
