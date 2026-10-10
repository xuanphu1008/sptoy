-- ToyLand v2 - nang cap SOA (giai doan 1)
-- Chay 1 lan tren CSDL toyland_v2. Viet de chay lai nhieu lan van an toan.

USE toyland_v2;
SET NAMES utf8mb4;

-- WP4: chong dat hang trung (Idempotency-Key)
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    idem_key      VARCHAR(80)  NOT NULL,
    subject       VARCHAR(60)  NOT NULL,
    request_hash  CHAR(64)     NOT NULL,
    state         VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    response_status INT        NULL,
    response_body MEDIUMTEXT   NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    DATETIME     NOT NULL,
    CONSTRAINT uq_idem_key_subject UNIQUE (idem_key, subject),
    INDEX idx_idem_expires (expires_at)
) ENGINE=InnoDB;

-- WP1: doi tac ben thu ba goi API /api/partner/v1 (khoa API + chu ky HMAC-SHA256)
CREATE TABLE IF NOT EXISTS partners (
    id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(120) NOT NULL,
    api_key            VARCHAR(40)  NOT NULL UNIQUE,
    secret             VARCHAR(80)  NOT NULL,
    active             TINYINT(1)   NOT NULL DEFAULT 1,
    rate_limit_per_min INT          NOT NULL DEFAULT 60,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at       DATETIME     NULL
) ENGINE=InnoDB;
