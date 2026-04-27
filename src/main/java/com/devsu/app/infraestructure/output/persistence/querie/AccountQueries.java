package com.devsu.app.infraestructure.output.persistence.querie;

import com.devsu.app.infraestructure.output.persistence.dto.AccountFilterDTO;

public final class AccountQueries {

    private AccountQueries() {}

    public static final String INSERT_ACCOUNT = """
        INSERT INTO banking.account (
            account_number, account_type, balance, daily_limit,
            status, customer_id, created_by, created_at
        ) VALUES (
            :accountNumber, :accountType, :balance, :dailyLimit,
            :status, :customerId, :createdBy, :createdAt
        )
        RETURNING id, account_number, account_type, balance, daily_limit,
                  status, customer_id, created_at
    """;

    public static final String FIND_BY_ACCOUNT_NUMBER = """
        SELECT * FROM banking.account_view
        WHERE account_number = :accountNumber
    """;

    public static final String DEACTIVATE_BY_ACCOUNT_NUMBER = """
        UPDATE banking.account SET status = 0
        WHERE account_number = :accountNumber
    """;

    public static final String UPDATE_ACCOUNT = """
        UPDATE banking.account SET
            account_type = :accountType,
            balance      = :balance,
            status       = :status,
            daily_limit  = :dailyLimit,
            updated_by   = :updatedBy,
            updated_at   = :updatedAt
        WHERE account_number = :accountNumber
        RETURNING id, account_number, account_type, balance, daily_limit, status, customer_id
    """;

    public static final String RESET_DAILY_LIMIT = """
        UPDATE banking.account
        SET daily_limit = :dailyLimit,
            updated_by  = 'SCHEDULER',
            updated_at  = NOW()
    """;

    public static String findAllFiltered(AccountFilterDTO filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT *, COUNT(*) OVER() AS total_count
            FROM banking.account_view
            WHERE 1=1
        """);

        if (filter.getAccountNumber() != null && !filter.getAccountNumber().isBlank()) {
            sql.append(" AND account_number ILIKE :accountNumber");
        }
        if (filter.getAccountType() != null && !filter.getAccountType().isBlank()) {
            sql.append(" AND account_type ILIKE :accountType");
        }
        if (filter.getCustomerIdentification() != null && !filter.getCustomerIdentification().isBlank()) {
            sql.append(" AND customer_identification ILIKE :customerIdentification");
        }

        sql.append(" ORDER BY id ASC LIMIT :limit OFFSET :offset");
        return sql.toString();
    }

}
