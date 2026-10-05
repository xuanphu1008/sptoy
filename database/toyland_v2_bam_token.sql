-- =====================================================================
--  TOYLAND v2 - DON DEP TOKEN CU SAU KHI CHUYEN SANG LUU BAN BAM (khong doi cau truc bang)
--  Dung khi da co CSDL toyland_v2. Chay TOAN BO file trong MySQL Workbench / phpMyAdmin.
--
--  Tu phien ban nay, backend chi luu SHA-256 (64 ky tu hex) cua:
--   * refresh token (bang refresh_tokens)
--   * ma dat lai mat khau (bang password_reset_tokens)
--  Cot token VARCHAR(100) van du chua -> KHONG can ALTER TABLE.
--
--  Token luu dang ro truoc day khong con dung duoc nua (backend bam token client gui len
--  roi moi tra cuu), nen xoa di cho sach:
--   * Moi nguoi dung se phai DANG NHAP LAI mot lan.
--   * Link "quen mat khau" da gui truoc do se het hieu luc -> yeu cau gui lai.
--  Chay lai file nay van an toan (chi dang xuat tat ca mot lan nua).
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

DELETE FROM refresh_tokens;

-- Ma dat lai mat khau cu dai 32 ky tu; ban bam moi dai dung 64 ky tu
DELETE FROM password_reset_tokens WHERE CHAR_LENGTH(token) <> 64;

SET SQL_SAFE_UPDATES = 1;
