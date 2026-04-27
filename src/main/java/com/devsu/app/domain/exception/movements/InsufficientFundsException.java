package com.devsu.app.domain.exception.movements;

import com.devsu.app.domain.exception.BusinessException;

public class InsufficientFundsException extends BusinessException {
    public InsufficientFundsException(String accountNumber) {
        super("INSUFFICIENT_FUNDS", "Saldo insuficiente en la cuenta: " + accountNumber);
    }
}
