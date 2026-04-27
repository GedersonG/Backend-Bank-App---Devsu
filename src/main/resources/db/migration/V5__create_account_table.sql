CREATE TABLE IF NOT EXISTS banking.account (
                                               id             BIGSERIAL PRIMARY KEY,
                                               account_number VARCHAR(20)    NOT NULL,
    account_type   VARCHAR(20)    NOT NULL,
    balance        DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    daily_limit    DECIMAL(15, 2) NOT NULL DEFAULT 1000.00,
    status         SMALLINT       NOT NULL DEFAULT 1,

    customer_id    BIGINT         NOT NULL REFERENCES banking.customer(id) ON DELETE RESTRICT,

    created_by     VARCHAR(50)    NOT NULL,
    updated_by     VARCHAR(50),
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP,

    CONSTRAINT chk_balance_positive CHECK (balance >= 0)
    );

COMMENT ON COLUMN banking.account.account_type IS 'AHORRO=Ahorro, CORRIENTE=Corriente';
        COMMENT ON COLUMN banking.account.status IS '1=ACTIVE, 0=INACTIVE, 2=BLOCKED';
COMMENT ON COLUMN banking.account.balance IS 'Saldo actual en moneda local';
COMMENT ON COLUMN banking.account.daily_limit IS 'Límite diario configurable por variable de entorno';

CREATE UNIQUE INDEX IF NOT EXISTS idx_account_number ON banking.account (account_number);
CREATE INDEX IF NOT EXISTS idx_account_customer_id ON banking.account (customer_id);
CREATE INDEX IF NOT EXISTS idx_account_status ON banking.account (status);
CREATE INDEX IF NOT EXISTS idx_account_type ON banking.account (account_type);
CREATE INDEX IF NOT EXISTS idx_account_created_at ON banking.account (created_at);