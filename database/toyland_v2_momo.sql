-- =====================================================================
--  TOYLAND v2 - NANG CAP CSDL: DOI CONG THANH TOAN VNPAY -> VI MOMO (khong xoa du lieu)
--  Dung khi da co CSDL toyland_v2 va KHONG muon chay lai toyland_v2.sql tu dau.
--  Chay TOAN BO file trong MySQL Workbench / phpMyAdmin. Chay lai nhieu lan van an toan.
--
--  Thay doi:
--   * orders.payment_method: rang buoc CHECK chi nhan 'COD' / 'MOMO'; don cu 'VNPAY' -> 'MOMO'
--     (backend khong con kieu VNPAY, de nguyen se loi khi doc don cu)
--   * payment_transactions: provider 'VNPAY' -> 'MOMO' de so cai, doi soat, thong ke nhat quan;
--     doi ma ket qua mau ('00' -> '0', '24' -> '1006' theo bang ma cua MoMo) va ghi chu tuong ung
--  Khong doi cau truc bang nao khac.
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

-- 1) Go rang buoc CHECK cu (neu co) - dung thu tuc de chay duoc tren ca MySQL 8 va MariaDB (XAMPP)
DROP PROCEDURE IF EXISTS tl_drop_chk_ord_pm;
DELIMITER $$
CREATE PROCEDURE tl_drop_chk_ord_pm()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND CONSTRAINT_NAME = 'chk_ord_pm') THEN
        ALTER TABLE orders DROP CONSTRAINT chk_ord_pm;
    END IF;
END$$
DELIMITER ;
CALL tl_drop_chk_ord_pm();
DROP PROCEDURE IF EXISTS tl_drop_chk_ord_pm;

-- 2) Doi du lieu
UPDATE orders SET payment_method = 'MOMO' WHERE payment_method = 'VNPAY';

UPDATE payment_transactions
   SET response_code = CASE response_code WHEN '00' THEN '0' WHEN '24' THEN '1006' ELSE response_code END,
       note = REPLACE(REPLACE(note, 'Khách huỷ giao dịch tại cổng VNPay', 'Khách huỷ giao dịch trên MoMo'),
                      'Thanh toán qua cổng VNPay', 'Thanh toán qua ví MoMo'),
       provider = 'MOMO'
 WHERE provider = 'VNPAY';

UPDATE orders SET internal_note = REPLACE(internal_note, 'VNPay', 'MoMo') WHERE internal_note LIKE '%VNPay%';
UPDATE order_status_history SET note = REPLACE(note, 'VNPay', 'MoMo') WHERE note LIKE '%VNPay%';

ALTER TABLE payment_transactions ALTER COLUMN provider SET DEFAULT 'MOMO';

-- 3) Gan lai rang buoc
ALTER TABLE orders ADD CONSTRAINT chk_ord_pm CHECK (payment_method IN ('COD','MOMO'));

SET SQL_SAFE_UPDATES = 1;

-- Kiem tra nhanh: ket qua phai la 0
SELECT COUNT(*) AS con_vnpay FROM orders WHERE payment_method = 'VNPAY';
