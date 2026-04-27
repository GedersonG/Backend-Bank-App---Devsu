package com.devsu.app.domain.exception.movements;

import com.devsu.app.domain.exception.BusinessException;

import java.math.BigDecimal;

public class DailyLimitExceededException extends BusinessException {
    public DailyLimitExceededException(String accountNumber, BigDecimal remaining) {
        super("DAILY_LIMIT_EXCEEDED",
                "Cupo diario excedido para la cuenta: " + accountNumber +
                        ". Cupo disponible: " + remaining);
    }
}
