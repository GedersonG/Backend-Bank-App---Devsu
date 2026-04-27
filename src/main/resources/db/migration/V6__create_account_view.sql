CREATE OR REPLACE VIEW banking.account_view AS
SELECT
    a.id,
    a.account_number,
    a.account_type,
    a.balance,
    a.daily_limit,
    a.status,
    a.customer_id,
    p.name           AS customer_name,
    p.identification AS customer_identification,
    c.client_id      AS client_id,
    a.created_by,
    a.updated_by,
    a.created_at,
    a.updated_at
FROM banking.account a
         JOIN banking.customer c ON c.id = a.customer_id
         JOIN banking.person p ON p.id = c.id;