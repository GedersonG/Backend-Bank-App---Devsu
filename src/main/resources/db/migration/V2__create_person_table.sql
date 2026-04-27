CREATE TABLE IF NOT EXISTS banking.person (
                                              id             BIGSERIAL PRIMARY KEY,
                                              name           VARCHAR(100) NOT NULL,
    gender         VARCHAR(20),
    age            INT          NOT NULL,
    identification VARCHAR(50)  NOT NULL,
    address        VARCHAR(255) NOT NULL,
    phone          VARCHAR(30)  NOT NULL,
    created_by     VARCHAR(50) NOT NULL,
    updated_by     VARCHAR(50),
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_person_identification ON banking.person (identification);
CREATE INDEX IF NOT EXISTS idx_person_name ON banking.person (name);
CREATE INDEX IF NOT EXISTS idx_person_created_at ON banking.person (created_at);
