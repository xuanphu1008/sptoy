-- =====================================================================
--  TOYLAND v2 - NANG CAP CSDL CHO CAC CHUC NANG MOI (khong xoa du lieu cu)
--  Dung khi da co CSDL toyland_v2 va KHONG muon chay lai toyland_v2.sql tu dau.
--  Chay TOAN BO file trong MySQL Workbench (Ctrl+Shift+Enter). Chay lai nhieu lan van an toan.
--
--  Bo sung:
--   * Nha cung cap (suppliers) + lien ket phieu nhap kho
--   * Gia von tren dong don hang (bao cao loi nhuan)
--   * Tu khoa cam + co "vi pham" tren danh gia (kiem duyet tu dong)
--   * Diem thuong khach hang than thiet (users.points, point_transactions, orders.points_*)
--   * Thong bao trong ung dung (notifications) + nhat ky thao tac (audit_logs)
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

DROP PROCEDURE IF EXISTS tl_add_col;
DROP PROCEDURE IF EXISTS tl_add_fk;
DELIMITER $$
CREATE PROCEDURE tl_add_col(IN t VARCHAR(64), IN c VARCHAR(64), IN ddl TEXT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) THEN
        SET @s = CONCAT('ALTER TABLE ', t, ' ADD COLUMN ', ddl);
        PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
    END IF;
END$$
CREATE PROCEDURE tl_add_fk(IN t VARCHAR(64), IN n VARCHAR(64), IN ddl TEXT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND CONSTRAINT_NAME = n) THEN
        SET @s = CONCAT('ALTER TABLE ', t, ' ADD CONSTRAINT ', n, ' ', ddl);
        PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
    END IF;
END$$
DELIMITER ;

-- ---------------------------------------------------------------- cot moi
CALL tl_add_col('users', 'points', 'points INT NOT NULL DEFAULT 0 AFTER deleted');
CALL tl_add_col('users', 'lifetime_points', 'lifetime_points INT NOT NULL DEFAULT 0 AFTER points');
CALL tl_add_col('orders', 'points_used', 'points_used INT NOT NULL DEFAULT 0 AFTER voucher_code');
CALL tl_add_col('orders', 'points_discount', 'points_discount DECIMAL(15,2) NOT NULL DEFAULT 0 AFTER points_used');
CALL tl_add_col('orders', 'points_earned', 'points_earned INT NOT NULL DEFAULT 0 AFTER points_discount');
CALL tl_add_col('order_items', 'cost_price', 'cost_price DECIMAL(15,2) NULL AFTER original_price');
CALL tl_add_col('reviews', 'flagged', 'flagged BOOLEAN NOT NULL DEFAULT FALSE AFTER hidden');
CALL tl_add_col('reviews', 'flag_reason', 'flag_reason VARCHAR(300) NULL AFTER flagged');
CALL tl_add_col('stock_receipts', 'supplier_id', 'supplier_id BIGINT NULL AFTER code');

-- ---------------------------------------------------------------- bang moi
CREATE TABLE IF NOT EXISTS suppliers (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(30)   NOT NULL UNIQUE,
    name            VARCHAR(200)  NOT NULL,
    contact_name    VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    address         VARCHAR(300),
    tax_code        VARCHAR(20),
    note            VARCHAR(500),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
CALL tl_add_fk('stock_receipts', 'fk_sr_supplier', 'FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL');

CREATE TABLE IF NOT EXISTS banned_keywords (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    keyword     VARCHAR(100) NOT NULL UNIQUE,
    created_by  VARCHAR(50),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS point_transactions (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    order_id       BIGINT       NULL,
    type           VARCHAR(20)  NOT NULL,
    points         INT          NOT NULL,
    balance_after  INT          NOT NULL,
    note           VARCHAR(300),
    created_by     VARCHAR(50),
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pt_user (user_id, id),
    CONSTRAINT fk_ptx_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_ptx_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    CONSTRAINT chk_ptx_type CHECK (type IN ('EARN','REDEEM','REFUND','ADJUST'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     VARCHAR(500),
    link        VARCHAR(300),
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_noti_user (user_id, is_read, id),
    CONSTRAINT fk_noti_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_logs (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    actor        VARCHAR(50)  NOT NULL,
    actor_role   VARCHAR(20),
    action       VARCHAR(30)  NOT NULL,
    entity_type  VARCHAR(40)  NOT NULL,
    entity_id    VARCHAR(40),
    summary      VARCHAR(500) NOT NULL,
    ip           VARCHAR(45),
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_time (created_at),
    INDEX idx_audit_entity (entity_type, entity_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- du lieu mau / bu du lieu cu
INSERT IGNORE INTO suppliers (code, name, contact_name, phone, email, address, tax_code) VALUES
('NCC-LEGO', 'Công ty TNHH LEGO Việt Nam', 'Nguyễn Hoàng Nam', '02838221234', 'sales@lego.vn', 'Tầng 12, 115 Nguyễn Huệ, Q.1, TP.HCM', '0315123456'),
('NCC-MATTEL', 'Mattel Việt Nam (Barbie, Hot Wheels, UNO)', 'Trần Minh Thư', '02839101122', 'order@mattel.vn', 'KCN Tân Tạo, Bình Tân, TP.HCM', '0309876543'),
('NCC-HASBRO', 'Hasbro Asia – Chi nhánh Hà Nội', 'Lê Quốc Việt', '02437654321', 'vn@hasbro.com', '72 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', '0108887766'),
('NCC-VTECH', 'VTech Electronics (phân phối bởi Tân Phú)', 'Phạm Hải Yến', '02835556677', 'yen.pham@tanphu.vn', '36 Cộng Hoà, Tân Bình, TP.HCM', '0304445566'),
('NCC-TEDDY', 'Xưởng thú bông Teddy Việt', 'Đỗ Thị Hạnh', '0987654321', 'teddyviet@gmail.com', 'Làng nghề Vạn Phúc, Hà Đông, Hà Nội', '0107778899'),
('NCC-ROBOKID', 'Công ty CP Giáo dục Robokid', 'Vũ Đức Anh', '0912003004', 'hello@robokid.vn', 'Số 5 Duy Tân, Cầu Giấy, Hà Nội', '0106665544');

-- phieu nhap cu ghi ten NCC dang chu -> gan voi NCC trung ten
UPDATE stock_receipts r JOIN suppliers s ON s.name = r.supplier SET r.supplier_id = s.id WHERE r.supplier_id IS NULL;

INSERT IGNORE INTO banned_keywords (keyword, created_by) VALUES
('lua dao', 'admin'), ('hang gia', 'admin'), ('hang nhai', 'admin'), ('do rac', 'admin'), ('scam', 'admin'),
('fake', 'admin'), ('dm', 'admin'), ('vcl', 'admin'), ('cho chet', 'admin'), ('ngu nhu', 'admin');

-- gia von cho cac dong don cu = gia von hien tai cua bien the
UPDATE order_items oi JOIN product_variants v ON v.id = oi.variant_id
SET oi.cost_price = v.cost_price WHERE oi.cost_price IS NULL;

-- tich diem cho cac don da HOAN TAT truoc day (1 diem / 10.000d tien hang)
UPDATE orders SET points_earned = FLOOR(subtotal / 10000) WHERE status = 'COMPLETED' AND points_earned = 0;
INSERT INTO point_transactions (user_id, order_id, type, points, balance_after, note, created_by, created_at)
SELECT o.user_id, o.id, 'EARN', o.points_earned, 0, CONCAT('Tích điểm đơn ', o.order_code), 'system', o.updated_at
FROM orders o
WHERE o.status = 'COMPLETED' AND o.points_earned > 0
  AND NOT EXISTS (SELECT 1 FROM point_transactions t WHERE t.order_id = o.id AND t.type = 'EARN');
-- so du sau moi giao dich (cong don theo thoi gian)
UPDATE point_transactions pt
JOIN (SELECT id, SUM(points) OVER (PARTITION BY user_id ORDER BY created_at, id) AS bal FROM point_transactions) x ON x.id = pt.id
SET pt.balance_after = x.bal;
UPDATE users u SET
    points = GREATEST(0, (SELECT COALESCE(SUM(t.points), 0) FROM point_transactions t WHERE t.user_id = u.id)),
    lifetime_points = (SELECT COALESCE(SUM(t.points), 0) FROM point_transactions t WHERE t.user_id = u.id AND t.type = 'EARN');

DROP PROCEDURE IF EXISTS tl_add_col;
DROP PROCEDURE IF EXISTS tl_add_fk;
SET SQL_SAFE_UPDATES = 1;

SELECT 'Nang cap xong' AS ket_qua,
       (SELECT COUNT(*) FROM suppliers) AS nha_cung_cap,
       (SELECT COUNT(*) FROM banned_keywords) AS tu_khoa_cam,
       (SELECT COUNT(*) FROM point_transactions) AS giao_dich_diem;
