-- =====================================================================
--  TOYLAND v2 - NANG CAP CSDL: XAC NHAN EMAIL KHI DANG KY (khong xoa du lieu cu)
--  Dung khi da co CSDL toyland_v2 va KHONG muon chay lai toyland_v2.sql tu dau.
--  Chay TOAN BO file trong MySQL Workbench (Ctrl+Shift+Enter). Chay lai nhieu lan van an toan.
--
--  Bo sung:
--   * users.email_verified_at (NULL = chua xac nhan, giong Laravel)
--   * Bang email_verification_tokens (luu SHA-256 cua token trong link xac nhan)
--   * Tai khoan dang co -> coi nhu DA xac nhan (chi lam 1 lan, luc vua them cot)
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

DROP PROCEDURE IF EXISTS tl_add_email_verified;
DELIMITER $$
CREATE PROCEDURE tl_add_email_verified()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email_verified_at') THEN
        ALTER TABLE users ADD COLUMN email_verified_at DATETIME NULL AFTER consent_at;
        -- Chi backfill dung lan them cot: chay lai file nay khong "xac nhan ho" tai khoan moi dang ky
        UPDATE users SET email_verified_at = created_at;
    END IF;
END$$
DELIMITER ;
CALL tl_add_email_verified();
DROP PROCEDURE IF EXISTS tl_add_email_verified;

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    email       VARCHAR(100) NOT NULL,
    token_hash  CHAR(64)     NOT NULL UNIQUE,
    expires_at  DATETIME     NOT NULL,
    used_at     DATETIME     NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_evt_user (user_id, created_at)
) ENGINE=InnoDB;

SELECT COUNT(*) AS tong_tai_khoan, SUM(email_verified_at IS NOT NULL) AS da_xac_nhan FROM users;
