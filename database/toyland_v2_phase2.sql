-- =====================================================================
-- ToyLand v2 - Giai doan 2 huong dich vu: outbox su kien + webhook cho doi tac
--  * bang outbox_events (transactional outbox)
--  * partners.webhook_url (dia chi nhan webhook cua doi tac)
-- Chay mot lan tren toyland_v2 (Aiven / Workbench). Chay lai nhieu lan van an toan.
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS outbox_events (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id        VARCHAR(36)  NOT NULL,
    type            VARCHAR(60)  NOT NULL,
    aggregate_id    VARCHAR(64)  NOT NULL,
    partner_id      BIGINT       NULL,
    payload         TEXT         NOT NULL,
    status          VARCHAR(10)  NOT NULL DEFAULT 'NEW',
    attempts        INT          NOT NULL DEFAULT 0,
    next_attempt_at DATETIME     NOT NULL,
    last_error      VARCHAR(500) NULL,
    created_at      DATETIME     NOT NULL,
    sent_at         DATETIME     NULL,
    CONSTRAINT uq_outbox_event_id UNIQUE (event_id),
    CONSTRAINT chk_outbox_status CHECK (status IN ('NEW','SENT','DEAD'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP PROCEDURE IF EXISTS tl_phase2;
DELIMITER $$
CREATE PROCEDURE tl_phase2()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbox_events' AND INDEX_NAME = 'idx_outbox_due') THEN
        CREATE INDEX idx_outbox_due ON outbox_events (status, next_attempt_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'partners' AND COLUMN_NAME = 'webhook_url') THEN
        ALTER TABLE partners ADD COLUMN webhook_url VARCHAR(300) NULL AFTER rate_limit_per_min;
    END IF;
END$$
DELIMITER ;
CALL tl_phase2();
DROP PROCEDURE IF EXISTS tl_phase2;

SELECT 'outbox_events' AS bang, COUNT(*) AS so_dong FROM outbox_events
UNION ALL
SELECT 'partners.webhook_url', COUNT(webhook_url) FROM partners;
