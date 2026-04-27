package com.devsu.app.domain.exception.account;

import com.devsu.app.domain.exception.BusinessException;

import java.math.BigDecimal;

public class InvalidBalanceException extends BusinessException {
    public InvalidBalanceException(BigDecimal balance) {
        super("INVALID_BALANCE", "El saldo no puede ser negativo: " + balance);
    }
}
