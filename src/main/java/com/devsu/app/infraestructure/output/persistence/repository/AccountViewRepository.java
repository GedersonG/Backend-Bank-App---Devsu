package com.devsu.app.infraestructure.output.persistence.repository;

import com.devsu.app.infraestructure.output.persistence.entity.AccountView;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountViewRepository extends ReactiveSortingRepository<AccountView, Long> {

    Mono<Boolean> existsByCustomerId(Long customerId);

    @Query("SELECT * FROM banking.account_view WHERE id > :cursor ORDER BY id ASC LIMIT :limit")
    Flux<AccountView> findAllPaginated(long cursor, int limit);
}
