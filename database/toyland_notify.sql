-- =====================================================================
-- toyland-notify: co so du lieu RIENG cua dich vu thong bao (khong dung chung bang voi toyland-api)
-- Chay mot lan. Tren Aiven: tao schema toyland_notify roi chay tiep cac bang ben duoi.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS toyland_notify CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE toyland_notify;
SET NAMES utf8mb4;

-- Su kien da nhan: khoa chinh la event_id nen nhan trung khong xu ly hai lan
CREATE TABLE IF NOT EXISTS processed_events (
    event_id    VARCHAR(36) NOT NULL PRIMARY KEY,
    type        VARCHAR(60) NOT NULL,
    received_at DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Moi dong la mot luot gui (email hoac webhook), co so lan thu va thoi diem thu ke tiep
CREATE TABLE IF NOT EXISTS deliveries (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id         VARCHAR(36)  NOT NULL,
    channel          VARCHAR(10)  NOT NULL,
    target           VARCHAR(300) NOT NULL,
    payload          MEDIUMTEXT   NOT NULL,
    secret           VARCHAR(120) NULL,
    status           VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    attempts         INT          NOT NULL DEFAULT 0,
    next_attempt_at  DATETIME     NOT NULL,
    last_error       VARCHAR(500) NULL,
    last_http_status INT          NULL,
    created_at       DATETIME     NOT NULL,
    sent_at          DATETIME     NULL,
    CONSTRAINT chk_delivery_channel CHECK (channel IN ('EMAIL','WEBHOOK')),
    CONSTRAINT chk_delivery_status  CHECK (status IN ('PENDING','SENT','DEAD')),
    INDEX idx_delivery_due (status, next_attempt_at),
    INDEX idx_delivery_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
