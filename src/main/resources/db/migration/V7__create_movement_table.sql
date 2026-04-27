CREATE TABLE IF NOT EXISTS banking.movement (
                                                id                    BIGSERIAL PRIMARY KEY,
                                                movement_date         TIMESTAMP      NOT NULL DEFAULT NOW(),
    movement_type         VARCHAR(10)    NOT NULL,
    value                 DECIMAL(15, 2) NOT NULL,
    balance               DECIMAL(15, 2) NOT NULL,

    account_id            BIGINT         NOT NULL REFERENCES banking.account(id) ON DELETE RESTRICT,
    reference_account_id  BIGINT         REFERENCES banking.account(id),
    reference_movement_id BIGINT         REFERENCES banking.movement(id),

    created_by            VARCHAR(50)    NOT NULL,
    updated_by            VARCHAR(50),
    created_at            TIMESTAMP      NOT NULL,
    updated_at            TIMESTAMP,

    CONSTRAINT chk_movement_type CHECK (movement_type IN ('Debito', 'Credito')),
    CONSTRAINT chk_value_positive CHECK (value > 0)
    );

COMMENT ON COLUMN banking.movement.movement_type IS 'Debito=resta del saldo, Credito=suma al saldo';
COMMENT ON COLUMN banking.movement.value IS 'Valor del movimiento, siempre positivo';
COMMENT ON COLUMN banking.movement.balance IS 'Saldo resultante de la cuenta después del movimiento';

CREATE INDEX IF NOT EXISTS idx_movement_account_id ON banking.movement (account_id);
CREATE INDEX IF NOT EXISTS idx_movement_date ON banking.movement (movement_date);
CREATE INDEX IF NOT EXISTS idx_movement_type ON banking.movement (movement_type);
CREATE INDEX IF NOT EXISTS idx_movement_created_at ON banking.movement (created_at);