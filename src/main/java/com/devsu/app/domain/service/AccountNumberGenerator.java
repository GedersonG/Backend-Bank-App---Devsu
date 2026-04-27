package com.devsu.app.domain.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private static final String BANK_CODE = "001";
    private final AtomicLong sequence = new AtomicLong(0);
    private final DatabaseClient databaseClient;

    @PostConstruct
    public void init() {
        databaseClient.sql("""
                SELECT COALESCE(MAX(CAST(SPLIT_PART(account_number, '-', 2) AS BIGINT)), 100000)
                AS last_seq FROM banking.account
            """)
                .fetch()
                .one()
                .map(row -> ((Number) row.get("last_seq")).longValue())
                .defaultIfEmpty(100000L)
                .doOnNext(sequence::set)
                .subscribe();
    }

    public String generate(String accountType) {
        long seq = sequence.incrementAndGet();
        String typeCode = "AHORRO".equals(accountType) ? "00" : "01";
        return String.format("%s-%06d-%s", BANK_CODE, seq, typeCode);
    }
}
