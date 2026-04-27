package com.devsu.app.infraestructure.output.persistence.adapter;

import com.devsu.app.domain.exception.customer.DuplicateClientIdException;
import com.devsu.app.domain.exception.customer.DuplicateIdentificationException;
import com.devsu.app.domain.model.Customer;
import com.devsu.app.domain.port.out.CustomerRepositoryPort;
import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;
import com.devsu.app.infraestructure.output.persistence.dto.PaginatedResult;
import com.devsu.app.infraestructure.output.persistence.mapper.CustomerPersistenceMapper;
import com.devsu.app.infraestructure.output.persistence.querie.CustomerQueries;
import com.devsu.app.infraestructure.output.persistence.repository.CustomerViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerViewRepository customerViewRepository;
    private final CustomerPersistenceMapper mapper;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<PaginatedResult<Customer>> findAll(CustomerFilterDTO filter, int pageSize, int offset) {
        String sql = CustomerQueries.findAllFiltered(filter);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("limit", pageSize)
                .bind("offset", offset);

        if (filter.getIdentification() != null && !filter.getIdentification().isBlank()) {
            spec = spec.bind("identification", "%" + filter.getIdentification() + "%");
        }
        if (filter.getName() != null && !filter.getName().isBlank()) {
            spec = spec.bind("name", "%" + filter.getName() + "%");
        }

        return spec.fetch().all()
                .collectList()
                .map(rows -> {
                    long totalCount = rows.isEmpty() ? 0L : ((Number) rows.getFirst().get("total_count")).longValue();
                    List<Customer> customers = rows.stream()
                            .map(mapper::rowToDomain)
                            .toList();
                    return new PaginatedResult<>(customers, totalCount);
                });
    }

    @Override
    public Mono<Boolean> existsByIdentification(String identification) {
        return databaseClient.sql(CustomerQueries.EXISTS_BY_IDENTIFICATION)
                .bind("identification", identification)
                .fetch()
                .one()
                .map(row -> (Boolean) row.get("exists"))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Customer> findByClientId(String clientId) {
        return customerViewRepository.findByClientId(clientId)
                .map(mapper::customerViewtoCustomer);
    }

    @Override
    @Transactional
    public Mono<Customer> save(Customer customer) {
        return databaseClient.sql(CustomerQueries.INSERT_CUSTOMER)
                .bindProperties(mapper.toInsertProperties(customer))
                .fetch()
                .one()
                .map(row -> {
                    customer.setId((Long) row.get("id"));
                    return customer;
                })
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                    if (message.contains("idx_person_identification")) {
                        return new DuplicateIdentificationException(customer.getIdentification());
                    }
                    if (message.contains("idx_customer_client_id")) {
                        return new DuplicateClientIdException(customer.getClientId());
                    }
                    return ex;
                });
    }

    @Override
    public Mono<Customer> update(Customer customer) {
        return databaseClient.sql(CustomerQueries.UPDATE_CUSTOMER)
                .bind("id", customer.getId())
                .bind("name", customer.getName())
                .bind("gender", customer.getGender() != null ? customer.getGender() : "")
                .bind("age", customer.getAge())
                .bind("identification", customer.getIdentification())
                .bind("address", customer.getAddress())
                .bind("phone", customer.getPhone())
                .bind("password", customer.getPassword())
                .bind("status", customer.getStatus())
                .bind("updatedBy", "BACKEND_APP")
                .bind("updatedAt", LocalDateTime.now())
                .fetch()
                .one()
                .map(row -> {
                    customer.setId((Long) row.get("id"));
                    return customer;
                })
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                    if (message.contains("idx_person_identification")) {
                        return new DuplicateIdentificationException(customer.getIdentification());
                    }
                    return ex;
                });
    }

    @Override
    public Mono<Void> deleteByClientId(String clientId) {
        return databaseClient.sql(CustomerQueries.DEACTIVATE_CUSTOMER)
                .bind("clientId", clientId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
