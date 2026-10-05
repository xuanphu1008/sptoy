-- =====================================================================
--  TOYLAND v2 - NANG CAP CSDL: QUAN LY THANH TOAN & DOI SOAT (khong xoa du lieu cu)
--  Dung khi da co CSDL toyland_v2 va KHONG muon chay lai toyland_v2.sql tu dau.
--  Chay TOAN BO file trong MySQL Workbench / phpMyAdmin. Chay lai nhieu lan van an toan.
--
--  Bo sung:
--   * Trang thai thanh toan moi REFUND_PENDING (cho hoan tien) cho bang orders
--   * payment_transactions thanh "so cai thu - chi": cot kind (PAYMENT/REFUND), note, created_by
--   * Tao but toan cho du lieu cu: tien COD da thu, tien da hoan cua cac don tra hang
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

DROP PROCEDURE IF EXISTS tl_upgrade_payments;
DELIMITER $$
CREATE PROCEDURE tl_upgrade_payments()
BEGIN
    -- 1. Cho phep trang thai REFUND_PENDING
    IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND CONSTRAINT_NAME = 'chk_ord_ps') THEN
        ALTER TABLE orders DROP CONSTRAINT chk_ord_ps;
    END IF;
    ALTER TABLE orders ADD CONSTRAINT chk_ord_ps
        CHECK (payment_status IN ('UNPAID','PAID','FAILED','REFUND_PENDING','REFUNDED'));

    -- 2. Cot moi cua so cai thanh toan
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_transactions' AND COLUMN_NAME = 'kind') THEN
        ALTER TABLE payment_transactions ADD COLUMN kind VARCHAR(10) NOT NULL DEFAULT 'PAYMENT' AFTER status;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_transactions' AND COLUMN_NAME = 'note') THEN
        ALTER TABLE payment_transactions ADD COLUMN note VARCHAR(300) NULL AFTER kind;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_transactions' AND COLUMN_NAME = 'created_by') THEN
        ALTER TABLE payment_transactions ADD COLUMN created_by VARCHAR(50) NULL AFTER note;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_transactions' AND CONSTRAINT_NAME = 'chk_pt_kind') THEN
        ALTER TABLE payment_transactions ADD CONSTRAINT chk_pt_kind CHECK (kind IN ('PAYMENT','REFUND'));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_transactions' AND INDEX_NAME = 'idx_pt_created') THEN
        CREATE INDEX idx_pt_created ON payment_transactions (created_at);
    END IF;
END$$
DELIMITER ;
CALL tl_upgrade_payments();
DROP PROCEDURE IF EXISTS tl_upgrade_payments;

-- 3. But toan cho du lieu cu (chi tao khi chua co -> chay lai khong nhan doi)
-- 3a. Tien COD da thu cua cac don da giao
INSERT INTO payment_transactions (order_id, provider, txn_ref, amount, status, kind, note, created_by, created_at, updated_at)
SELECT o.id, 'COD', CONCAT('COD-', o.order_code), o.total_amount, 'SUCCESS', 'PAYMENT',
       'Thu tiền mặt khi giao hàng (COD)', 'system',
       COALESCE(o.delivered_at, o.paid_at, o.created_at), COALESCE(o.delivered_at, o.paid_at, o.created_at)
FROM orders o
WHERE o.payment_method = 'COD' AND o.payment_status IN ('PAID', 'REFUNDED')
  AND NOT EXISTS (SELECT 1 FROM payment_transactions t WHERE t.order_id = o.id AND t.kind = 'PAYMENT' AND t.status = 'SUCCESS');

-- 3b. Tien da hoan cua cac don da o trang thai "Da hoan tien"
INSERT INTO payment_transactions (order_id, provider, txn_ref, amount, status, kind, note, created_by, created_at, updated_at)
SELECT o.id, 'BANK_TRANSFER', CONCAT('RF-', o.order_code, '-0'), COALESCE(r.refund_amount, o.total_amount), 'SUCCESS', 'REFUND',
       'Hoàn tiền (ghi nhận bù khi nâng cấp sổ đối soát)', 'system',
       COALESCE(r.processed_at, o.updated_at), COALESCE(r.processed_at, o.updated_at)
FROM orders o
LEFT JOIN return_requests r ON r.order_id = o.id
WHERE o.payment_status = 'REFUNDED'
  AND NOT EXISTS (SELECT 1 FROM payment_transactions t WHERE t.order_id = o.id AND t.kind = 'REFUND' AND t.status = 'SUCCESS');

SELECT kind, provider, status, COUNT(*) AS so_giao_dich, SUM(amount) AS tong_tien
FROM payment_transactions GROUP BY kind, provider, status;
