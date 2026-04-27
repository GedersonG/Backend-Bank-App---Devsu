package com.devsu.app.infraestructure.output.persistence.querie;

import com.devsu.app.infraestructure.output.persistence.dto.CustomerFilterDTO;

public final class CustomerQueries {

    private CustomerQueries() {}

    public static final String INSERT_CUSTOMER = """
        WITH person_insert AS (
            INSERT INTO banking.person (name, gender, age, identification, address, phone, created_by, created_at)
            VALUES (:name, :gender, :age, :identification, :address, :phone, :createdBy, :createdAt)
            RETURNING id
        )
        INSERT INTO banking.customer (id, client_id, password, status)
        SELECT id, :clientId, :password, :status FROM person_insert
        RETURNING id, client_id, password, status
    """;

    public static final String EXISTS_BY_IDENTIFICATION = """
        SELECT EXISTS(
            SELECT 1 FROM banking.person
            WHERE identification = :identification
        ) AS exists
    """;

    public static final String UPDATE_CUSTOMER = """
        WITH person_update AS (
            UPDATE banking.person SET
                name           = :name,
                gender         = :gender,
                age            = :age,
                identification = :identification,
                address        = :address,
                phone          = :phone,
                updated_by     = :updatedBy,
                updated_at     = :updatedAt
            WHERE id = :id
            RETURNING id
        )
        UPDATE banking.customer SET
            password = :password,
            status   = :status
        FROM person_update
        WHERE banking.customer.id = person_update.id
        RETURNING banking.customer.id, client_id, password, status
    """;

    public static final String DEACTIVATE_CUSTOMER = """
        UPDATE banking.customer SET status = 0
        WHERE client_id = :clientId
    """;

    public static String findAllFiltered(CustomerFilterDTO filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT *, COUNT(*) OVER() AS total_count
            FROM banking.customer_view
            WHERE 1=1
        """);

        if (filter.getIdentification() != null && !filter.getIdentification().isBlank()) {
            sql.append(" AND identification ILIKE :identification");
        }
        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" AND name ILIKE :name");
        }

        sql.append(" ORDER BY id ASC LIMIT :limit OFFSET :offset");
        return sql.toString();
    }
}
