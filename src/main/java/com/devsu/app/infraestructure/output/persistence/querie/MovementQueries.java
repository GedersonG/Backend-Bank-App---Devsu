package com.devsu.app.infraestructure.output.persistence.querie;

import com.devsu.app.infraestructure.output.persistence.dto.MovementFilterDTO;

public final class MovementQueries {

    private MovementQueries() {}

    public static final String INSERT_MOVEMENT = """
        INSERT INTO banking.movement (
            movement_date, movement_type, value, balance,
            account_id, reference_account_id, reference_movement_id,
            created_by, created_at
        ) VALUES (
            :movementDate, :movementType, :value, :balance,
            :accountId, :referenceAccountId, :referenceMovementId,
            :createdBy, :createdAt
        )
        RETURNING id, movement_date, balance
    """;

    public static final String FIND_ACCOUNT_BY_NUMBER = """
        SELECT id, balance, status, daily_limit
        FROM banking.account
        WHERE account_number = :accountNumber
    """;

    public static final String UPDATE_ACCOUNT_BALANCE = """
        UPDATE banking.account
        SET balance    = :balance,
            updated_by = 'MOVEMENT',
            updated_at = NOW()
        WHERE id = :accountId
    """;

    public static final String UPDATE_ACCOUNT_BALANCE_AND_LIMIT = """
        UPDATE banking.account
        SET balance     = :balance,
            daily_limit = :dailyLimit,
            updated_by  = 'MOVEMENT',
            updated_at  = NOW()
        WHERE id = :accountId
        RETURNING balance, daily_limit
    """;

    public static final String UPDATE_ACCOUNT_BALANCE_ONLY = """
        UPDATE banking.account
        SET balance    = :balance,
            updated_by = 'MOVEMENT',
            updated_at = NOW()
        WHERE id = :accountId
    """;

    public static final String FIND_ACCOUNT_BY_ID = """
        SELECT id, balance, status, daily_limit
        FROM banking.account
        WHERE id = :accountId
    """;

    public static final String FIND_MOVEMENT_BY_ID = """
        SELECT * FROM banking.movement_view
        WHERE id = :id
    """;

    public static final String REVERSE_MOVEMENT = """
        INSERT INTO banking.movement (
            movement_date, movement_type, value, balance,
            account_id, reference_account_id, reference_movement_id,
            created_by, created_at
        ) VALUES (
            NOW(), :movementType, :value, :balance,
            :accountId, :referenceAccountId, :originalMovementId,
            'REVERSAL', NOW()
        )
        RETURNING id, movement_date, balance
    """;

    public static String findAllFiltered(MovementFilterDTO filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT *, COUNT(*) OVER() AS total_count
            FROM banking.movement_view
            WHERE 1=1
        """);

        if (filter.getAccountNumber() != null && !filter.getAccountNumber().isBlank()) {
            sql.append(" AND account_number ILIKE :accountNumber");
        }
        if (filter.getStartDate() != null) {
            sql.append(" AND movement_date >= :startDate");
        }
        if (filter.getEndDate() != null) {
            sql.append(" AND movement_date <= :endDate");
        }

        sql.append(" ORDER BY movement_date DESC LIMIT :limit OFFSET :offset");
        return sql.toString();
    }
}
