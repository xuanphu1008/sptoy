-- =====================================================================
-- ToyLand v2 - Cho phep doi tac (API ben thu ba) dat hang
--  * orders.user_id cho phep NULL (don do doi tac dat khong co tai khoan khach)
--  * them orders.partner_id, orders.partner_ref (ma don ben doi tac, khong trung theo tung doi tac)
--  * orders.payment_method nhan them 'PARTNER'
-- Chay mot lan tren toyland_v2 (Aiven / Workbench). Chay lai nhieu lan van an toan.
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;

-- 1) user_id cho phep NULL
ALTER TABLE orders MODIFY user_id BIGINT NULL;

-- 2) Cot moi (kiem tra truoc de chay lai khong loi)
DROP PROCEDURE IF EXISTS tl_partner_orders;
DELIMITER $$
CREATE PROCEDURE tl_partner_orders()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'partner_id') THEN
        ALTER TABLE orders ADD COLUMN partner_id BIGINT NULL AFTER user_id;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'partner_ref') THEN
        ALTER TABLE orders ADD COLUMN partner_ref VARCHAR(64) NULL AFTER partner_id;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND CONSTRAINT_NAME = 'fk_orders_partner') THEN
        ALTER TABLE orders ADD CONSTRAINT fk_orders_partner FOREIGN KEY (partner_id) REFERENCES partners (id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'uq_orders_partner_ref') THEN
        -- NULL khong tinh trung nen don cua khach thuong khong bi anh huong
        CREATE UNIQUE INDEX uq_orders_partner_ref ON orders (partner_id, partner_ref);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND CONSTRAINT_NAME = 'chk_ord_pm') THEN
        ALTER TABLE orders DROP CONSTRAINT chk_ord_pm;
    END IF;
END$$
DELIMITER ;
CALL tl_partner_orders();
DROP PROCEDURE IF EXISTS tl_partner_orders;

-- 3) Rang buoc phuong thuc thanh toan
ALTER TABLE orders ADD CONSTRAINT chk_ord_pm CHECK (payment_method IN ('COD','MOMO','PARTNER'));

SELECT COLUMN_NAME, IS_NULLABLE FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME IN ('user_id','partner_id','partner_ref');
