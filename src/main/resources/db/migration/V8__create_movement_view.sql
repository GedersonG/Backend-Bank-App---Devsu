CREATE OR REPLACE VIEW banking.movement_view AS
SELECT
    m.id,
    m.movement_date,
    m.movement_type,
    m.value,
    m.balance,
    m.account_id,
    a.account_number,
    a.account_type,
    p.name            AS customer_name,
    p.identification  AS customer_identification,
    c.client_id,

    ra.account_number AS reference_account_number,
    rp.name           AS reference_customer_name,

    m.reference_account_id,
    m.reference_movement_id,
    m.created_by,
    m.updated_by,
    m.created_at,
    m.updated_at
FROM banking.movement m
         JOIN banking.account a   ON a.id = m.account_id
         JOIN banking.customer c  ON c.id = a.customer_id
         JOIN banking.person p    ON p.id = c.id
         LEFT JOIN banking.account ra  ON ra.id = m.reference_account_id
         LEFT JOIN banking.customer rc ON rc.id = ra.customer_id
         LEFT JOIN banking.person rp   ON rp.id = rc.id;