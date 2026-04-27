package com.devsu.app.infraestructure.output.persistence.querie;

public final class ReportQueries {

    private ReportQueries() {}

    public static final String ACCOUNT_STATEMENT = """
        SELECT
            a.account_number,
            a.account_type,
            a.balance                                          AS current_balance,
            COALESCE(SUM(CASE WHEN m.movement_type = 'Debito'
                THEN m.value ELSE 0 END), 0)                  AS total_debits,
            COALESCE(SUM(CASE WHEN m.movement_type = 'Credito'
                THEN m.value ELSE 0 END), 0)                  AS total_credits,
            COUNT(m.id)                                        AS total_movements
        FROM banking.account a
            JOIN banking.customer c ON c.id = a.customer_id
            JOIN banking.person p ON p.id = c.id
            LEFT JOIN banking.movement m ON m.account_id = a.id
                AND m.movement_date BETWEEN :startDate AND :endDate
        WHERE c.client_id = :clientId
        GROUP BY a.id, a.account_number, a.account_type, a.balance
        ORDER BY a.account_number
    """;
}
