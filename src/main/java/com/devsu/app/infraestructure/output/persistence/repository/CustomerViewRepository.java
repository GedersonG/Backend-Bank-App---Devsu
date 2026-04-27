package com.devsu.app.infraestructure.output.persistence.repository;

import com.devsu.app.infraestructure.output.persistence.entity.CustomerView;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerViewRepository extends ReactiveSortingRepository<CustomerView, Long> {

    Mono<CustomerView> findByClientId(String clientId);

    @Query("SELECT * FROM banking.customer_view WHERE id > :cursor ORDER BY id ASC LIMIT :limit")
    Flux<CustomerView> findAllPaginated(long cursor, int limit);
}
