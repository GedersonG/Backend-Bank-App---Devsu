CREATE TABLE IF NOT EXISTS banking.customer (
                                                id        BIGINT PRIMARY KEY REFERENCES banking.person(id) ON DELETE CASCADE,
    client_id VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    status    SMALLINT     NOT NULL DEFAULT 1
    );

COMMENT ON COLUMN banking.customer.status IS '1=ACTIVE, 0=INACTIVE, 2=CANCELLED, 3=REJECTED';

CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_client_id ON banking.customer (client_id);
CREATE INDEX IF NOT EXISTS idx_customer_status ON banking.customer (status);