package com.devsu.app.infraestructure.input.adapter.rest.schedule;

import com.devsu.app.domain.port.in.AccountUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLimitScheduler {

    private final AccountUseCase accountUseCase;

    @Scheduled(cron = "${banking.account.reset-cron}")
    public void resetDailyLimits() {
        log.info("Starting daily limit reset...");
        accountUseCase.resetDailyLimits()
                .doOnSuccess(count ->
                        log.info("Daily limit reset completed. Accounts updated: {}", count))
                .doOnError(error ->
                        log.error("Daily limit reset failed: {}", error.getMessage()))
                .subscribe();
    }
}
