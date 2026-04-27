package com.devsu.app.domain.port.in;

import com.devsu.app.domain.model.CustomerStatement;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ReportUseCase {
    Mono<CustomerStatement> generateStatement(
            String clientId, LocalDateTime startDate, LocalDateTime endDate);
}
