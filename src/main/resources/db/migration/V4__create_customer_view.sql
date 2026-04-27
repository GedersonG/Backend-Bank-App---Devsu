CREATE OR REPLACE VIEW banking.customer_view AS
SELECT
    p.id,
    p.name,
    p.gender,
    p.age,
    p.identification,
    p.address,
    p.phone,
    p.created_by,
    p.updated_by,
    p.created_at,
    p.updated_at,
    c.client_id,
    c.password,
    c.status
FROM banking.customer c JOIN banking.person p ON p.id = c.id;