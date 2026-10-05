-- =====================================================================
--  TOYLAND v2 - DU LIEU MAU BO SUNG: 62 SAN PHAM + BIEN THE
--  Chay SAU khi da co CSDL toyland_v2 (file toyland_v2.sql).
--  * Khong xoa du lieu cu (don hang, tai khoan, san pham ban tu tao van giu nguyen).
--  * Chay lai nhieu lan khong bi nhan doi: san pham trung slug se duoc bo qua.
--  * Them: 1 danh muc con, 9 thuong hieu, 3 nhan vat, 62 san pham, 122 bien the,
--          anh, thong so, lich su kho (INITIAL).
--  Sinh tu dong boi addon.py (products_more.py) - khong sua tay.
-- =====================================================================
USE toyland_v2;
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;
START TRANSACTION;

-- 1. Danh muc, thuong hieu, nhan vat moi
SET @parent = (SELECT id FROM categories WHERE slug = 'do-choi-giao-duc');
INSERT INTO categories (parent_id, name, slug, description, image_url, icon, sort_order)
SELECT @parent, 'Nghệ thuật & Sáng tạo', 'nghe-thuat-sang-tao', NULL, 'https://placehold.co/300x300/A5D6A7/263238/png?text=Nghe+Thuat++Sang+Tao', NULL, (SELECT COALESCE(MAX(c.sort_order), 0) + 1 FROM categories c)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'nghe-thuat-sang-tao');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Fisher-Price', 'fisher-price', 'Mỹ', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=FISHERPRICE', 'Thương hiệu Fisher-Price (Mỹ)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'fisher-price' OR name = 'Fisher-Price');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Play-Doh', 'play-doh', 'Mỹ', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=PLAYDOH', 'Thương hiệu Play-Doh (Mỹ)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'play-doh' OR name = 'Play-Doh');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Bandai', 'bandai', 'Nhật Bản', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=BANDAI', 'Thương hiệu Bandai (Nhật Bản)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'bandai' OR name = 'Bandai');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Tomica', 'tomica', 'Nhật Bản', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=TOMICA', 'Thương hiệu Tomica (Nhật Bản)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'tomica' OR name = 'Tomica');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Sylvanian Families', 'sylvanian-families', 'Nhật Bản', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=SYLVANIAN+FAMILIES', 'Thương hiệu Sylvanian Families (Nhật Bản)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'sylvanian-families' OR name = 'Sylvanian Families');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Rubik''s', 'rubik-s', 'Hungary', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=RUBIKS', 'Thương hiệu Rubik''s (Hungary)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'rubik-s' OR name = 'Rubik''s');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Mideer', 'mideer', 'Trung Quốc', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=MIDEER', 'Thương hiệu Mideer (Trung Quốc)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'mideer' OR name = 'Mideer');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'Rastar', 'rastar', 'Trung Quốc', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=RASTAR', 'Thương hiệu Rastar (Trung Quốc)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'rastar' OR name = 'Rastar');
INSERT INTO brands (name, slug, country, logo_url, description)
SELECT 'BoardGame VN', 'boardgame-vn', 'Việt Nam', 'https://placehold.co/200x200/ECEFF1/37474F/png?text=BOARDGAME+VN', 'Thương hiệu BoardGame VN (Việt Nam)'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'boardgame-vn' OR name = 'BoardGame VN');
INSERT INTO franchises (name, slug, image_url, description)
SELECT 'Doraemon', 'doraemon', 'https://placehold.co/300x300/1E88E5/FFFFFF/png?text=Doraemon', 'Đồ chơi chủ đề Doraemon'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM franchises WHERE slug = 'doraemon' OR name = 'Doraemon');
INSERT INTO franchises (name, slug, image_url, description)
SELECT 'Gundam', 'gundam', 'https://placehold.co/300x300/455A64/FFFFFF/png?text=Gundam', 'Đồ chơi chủ đề Gundam'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM franchises WHERE slug = 'gundam' OR name = 'Gundam');
INSERT INTO franchises (name, slug, image_url, description)
SELECT 'Minecraft', 'minecraft', 'https://placehold.co/300x300/558B2F/FFFFFF/png?text=Minecraft', 'Đồ chơi chủ đề Minecraft'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM franchises WHERE slug = 'minecraft' OR name = 'Minecraft');

-- 2. San pham + bien the + anh + thong so + nhan vat + lich su kho

-- [01] LEGO City Trạm cảnh sát 60316
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-thanh-pho');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO City Trạm cảnh sát 60316', 'lego-city-tram-canh-sat-60316', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO City Trạm cảnh sát 60316 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 48, 38, 9, NULL, NULL, 'ACTIVE', FALSE, FALSE, 1599000, 1599000, 15, 90, 0, 0, 'lego city tram canh sat 60316 lego leg-0040', 'LEGO City Trạm cảnh sát 60316 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 69120 MINUTE), DATE_SUB(NOW(), INTERVAL 69120 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-city-tram-canh-sat-60316');
SET @p = (SELECT id FROM products WHERE slug = 'lego-city-tram-canh-sat-60316');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0040' AS sku, '8930000000055' AS barcode, NULL AS option1, NULL AS option2, 1599000 AS price, NULL AS compare_at_price, 959000 AS cost_price, 15 AS stock_quantity, 1300 AS weight_gram, 'https://placehold.co/600x600/1E88E5/3E2723/png?text=LEGO+City+Trm+cnh+s' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/1E88E5/3E2723/png?text=LEGO+City+Trm+cnh+st' AS url, 'LEGO City Trạm cảnh sát 60316' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+City+Trm+cnh+st++2' AS url, 'LEGO City Trạm cảnh sát 60316' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+City+Trm+cnh+st++3' AS url, 'LEGO City Trạm cảnh sát 60316' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+City+Trm+cnh+st++4' AS url, 'LEGO City Trạm cảnh sát 60316' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '668' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chủ đề' AS spec_key, 'LEGO City' AS spec_value, 1 AS sort_order
  UNION ALL SELECT 'Số nhân vật minifigure' AS spec_key, '5' AS spec_value, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 69120 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [02] LEGO Friends Nhà cây tình bạn 42652
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-thanh-pho');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Friends Nhà cây tình bạn 42652', 'lego-friends-nha-cay-tinh-ban-42652', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Friends Nhà cây tình bạn 42652 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'GIRL', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 48, 38, 11, NULL, NULL, 'ACTIVE', TRUE, FALSE, 2399000, 2399000, 6, 46, 0, 0, 'lego friends nha cay tinh ban 42652 lego leg-0041', 'LEGO Friends Nhà cây tình bạn 42652 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 17280 MINUTE), DATE_SUB(NOW(), INTERVAL 17280 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-friends-nha-cay-tinh-ban-42652');
SET @p = (SELECT id FROM products WHERE slug = 'lego-friends-nha-cay-tinh-ban-42652');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0041' AS sku, '8930000000056' AS barcode, NULL AS option1, NULL AS option2, 2399000 AS price, 2699000 AS compare_at_price, 1439000 AS cost_price, 6 AS stock_quantity, 1700 AS weight_gram, 'https://placehold.co/600x600/F48FB1/3E2723/png?text=LEGO+Friends+Nh+cy+t' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F48FB1/3E2723/png?text=LEGO+Friends+Nh+cy+tn' AS url, 'LEGO Friends Nhà cây tình bạn 42652' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Friends+Nh+cy+tn+2' AS url, 'LEGO Friends Nhà cây tình bạn 42652' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Friends+Nh+cy+tn+3' AS url, 'LEGO Friends Nhà cây tình bạn 42652' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Friends+Nh+cy+tn+4' AS url, 'LEGO Friends Nhà cây tình bạn 42652' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '1.114' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chủ đề' AS spec_key, 'LEGO Friends' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 17280 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [03] LEGO Minecraft Ngôi nhà hình con ếch 21256
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-thanh-pho');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Minecraft Ngôi nhà hình con ếch 21256', 'lego-minecraft-ngoi-nha-hinh-con-ech-21256', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Minecraft Ngôi nhà hình con ếch 21256 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 799000, 799000, 20, 109, 0, 0, 'lego minecraft ngoi nha hinh con ech 21256 lego minecraft leg-0042', 'LEGO Minecraft Ngôi nhà hình con ếch 21256 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 28800 MINUTE), DATE_SUB(NOW(), INTERVAL 28800 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-minecraft-ngoi-nha-hinh-con-ech-21256');
SET @p = (SELECT id FROM products WHERE slug = 'lego-minecraft-ngoi-nha-hinh-con-ech-21256');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0042' AS sku, '8930000000057' AS barcode, NULL AS option1, NULL AS option2, 799000 AS price, NULL AS compare_at_price, 479000 AS cost_price, 20 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/7CB342/3E2723/png?text=LEGO+Minecraft+Ngi+nh' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/7CB342/3E2723/png?text=LEGO+Minecraft+Ngi+nh' AS url, 'LEGO Minecraft Ngôi nhà hình con ếch 21256' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Minecraft+Ngi+nh++2' AS url, 'LEGO Minecraft Ngôi nhà hình con ếch 21256' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Minecraft+Ngi+nh++3' AS url, 'LEGO Minecraft Ngôi nhà hình con ếch 21256' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Minecraft+Ngi+nh++4' AS url, 'LEGO Minecraft Ngôi nhà hình con ếch 21256' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '400' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chủ đề' AS spec_key, 'LEGO Minecraft' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'minecraft';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 28800 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [04] LEGO City Tàu hỏa chở hàng 60336
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-thanh-pho');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO City Tàu hỏa chở hàng 60336', 'lego-city-tau-hoa-cho-hang-60336', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO City Tàu hỏa chở hàng 60336 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 84, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, '6 pin AAA (không kèm)', TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 58, 48, 12, NULL, NULL, 'ACTIVE', FALSE, FALSE, 3999000, 3999000, 4, 174, 0, 0, 'lego city tau hoa cho hang 60336 lego leg-0043', 'LEGO City Tàu hỏa chở hàng 60336 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 12960 MINUTE), DATE_SUB(NOW(), INTERVAL 12960 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-city-tau-hoa-cho-hang-60336');
SET @p = (SELECT id FROM products WHERE slug = 'lego-city-tau-hoa-cho-hang-60336');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0043' AS sku, '8930000000058' AS barcode, NULL AS option1, NULL AS option2, 3999000 AS price, NULL AS compare_at_price, 2399000 AS cost_price, 4 AS stock_quantity, 2600 AS weight_gram, 'https://placehold.co/600x600/F4511E/3E2723/png?text=LEGO+City+Tu+ha+ch' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F4511E/3E2723/png?text=LEGO+City+Tu+ha+ch+h' AS url, 'LEGO City Tàu hỏa chở hàng 60336' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+City+Tu+ha+ch+h+2' AS url, 'LEGO City Tàu hỏa chở hàng 60336' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+City+Tu+ha+ch+h+3' AS url, 'LEGO City Tàu hỏa chở hàng 60336' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+City+Tu+ha+ch+h+4' AS url, 'LEGO City Tàu hỏa chở hàng 60336' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '1.153' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Đường ray' AS spec_key, '20 thanh ray cong & thẳng' AS spec_value, 1 AS sort_order
  UNION ALL SELECT 'Điều khiển' AS spec_key, 'Bluetooth / app Powered Up' AS spec_value, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 12960 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [05] LEGO City Bộ xe mini  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-thanh-pho');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO City Bộ xe mini', 'lego-city-bo-xe-mini', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO City Bộ xe mini chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 60, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, 'Mẫu xe', NULL, 'ACTIVE', FALSE, FALSE, 239000, 259000, 52, 20, 0, 0, 'lego city bo xe mini lego leg-0044-01 leg-0044-02 leg-0044-03', 'LEGO City Bộ xe mini | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 100800 MINUTE), DATE_SUB(NOW(), INTERVAL 100800 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-city-bo-xe-mini');
SET @p = (SELECT id FROM products WHERE slug = 'lego-city-bo-xe-mini');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0044-01' AS sku, '8930000000059' AS barcode, 'Xe cảnh sát' AS option1, NULL AS option2, 239000 AS price, NULL AS compare_at_price, 143000 AS cost_price, 30 AS stock_quantity, 200 AS weight_gram, 'https://placehold.co/600x600/42A5F5/3E2723/png?text=Xe+cnh+st' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'LEG-0044-02' AS sku, '8930000000060' AS barcode, 'Xe cứu hỏa' AS option1, NULL AS option2, 239000 AS price, NULL AS compare_at_price, 143000 AS cost_price, 22 AS stock_quantity, 200 AS weight_gram, 'https://placehold.co/600x600/42A5F5/3E2723/png?text=Xe+cu+ha' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'LEG-0044-03' AS sku, '8930000000061' AS barcode, 'Xe chở rác' AS option1, NULL AS option2, 259000 AS price, NULL AS compare_at_price, 155000 AS cost_price, 0 AS stock_quantity, 220 AS weight_gram, 'https://placehold.co/600x600/42A5F5/3E2723/png?text=Xe+ch+rc' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/42A5F5/3E2723/png?text=LEGO+City+B+xe+mini' AS url, 'LEGO City Bộ xe mini' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+City+B+xe+mini+2' AS url, 'LEGO City Bộ xe mini' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+City+B+xe+mini+3' AS url, 'LEGO City Bộ xe mini' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+City+B+xe+mini+4' AS url, 'LEGO City Bộ xe mini' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '60 - 90' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chủ đề' AS spec_key, 'LEGO City' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 100800 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [06] LEGO Technic Mô tô Ducati Panigale V4 R 42107
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-ky-thuat');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Technic Mô tô Ducati Panigale V4 R 42107', 'lego-technic-mo-to-ducati-panigale-v4-r-42107', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Technic Mô tô Ducati Panigale V4 R 42107 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 120, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 1290000, 1290000, 8, 26, 0, 0, 'lego technic mo to ducati panigale v4 r 42107 lego leg-0045', 'LEGO Technic Mô tô Ducati Panigale V4 R 42107 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 122400 MINUTE), DATE_SUB(NOW(), INTERVAL 122400 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-technic-mo-to-ducati-panigale-v4-r-42107');
SET @p = (SELECT id FROM products WHERE slug = 'lego-technic-mo-to-ducati-panigale-v4-r-42107');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0045' AS sku, '8930000000062' AS barcode, NULL AS option1, NULL AS option2, 1290000 AS price, NULL AS compare_at_price, 774000 AS cost_price, 8 AS stock_quantity, 950 AS weight_gram, 'https://placehold.co/600x600/D32F2F/3E2723/png?text=LEGO+Technic+M+t+Duc' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/D32F2F/3E2723/png?text=LEGO+Technic+M+t+Ducat' AS url, 'LEGO Technic Mô tô Ducati Panigale V4 R 42107' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Technic+M+t+Ducat+2' AS url, 'LEGO Technic Mô tô Ducati Panigale V4 R 42107' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Technic+M+t+Ducat+3' AS url, 'LEGO Technic Mô tô Ducati Panigale V4 R 42107' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Technic+M+t+Ducat+4' AS url, 'LEGO Technic Mô tô Ducati Panigale V4 R 42107' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '646' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Hộp số' AS spec_key, '2 cấp mô phỏng' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 122400 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [07] LEGO Creator 3in1 Rồng đỏ huyền thoại 31145
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-ky-thuat');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Creator 3in1 Rồng đỏ huyền thoại 31145', 'lego-creator-3in1-rong-do-huyen-thoai-31145', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Creator 3in1 Rồng đỏ huyền thoại 31145 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 84, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, NULL, NULL, 'ACTIVE', TRUE, FALSE, 549000, 549000, 25, 145, 0, 0, 'lego creator 3in1 rong do huyen thoai 31145 lego leg-0046', 'LEGO Creator 3in1 Rồng đỏ huyền thoại 31145 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 47520 MINUTE), DATE_SUB(NOW(), INTERVAL 47520 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-creator-3in1-rong-do-huyen-thoai-31145');
SET @p = (SELECT id FROM products WHERE slug = 'lego-creator-3in1-rong-do-huyen-thoai-31145');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0046' AS sku, '8930000000063' AS barcode, NULL AS option1, NULL AS option2, 549000 AS price, NULL AS compare_at_price, 329000 AS cost_price, 25 AS stock_quantity, 400 AS weight_gram, 'https://placehold.co/600x600/E53935/3E2723/png?text=LEGO+Creator+3in1+Rng' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/E53935/3E2723/png?text=LEGO+Creator+3in1+Rng' AS url, 'LEGO Creator 3in1 Rồng đỏ huyền thoại 31145' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Creator+3in1+Rng++2' AS url, 'LEGO Creator 3in1 Rồng đỏ huyền thoại 31145' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Creator+3in1+Rng++3' AS url, 'LEGO Creator 3in1 Rồng đỏ huyền thoại 31145' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Creator+3in1+Rng++4' AS url, 'LEGO Creator 3in1 Rồng đỏ huyền thoại 31145' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '149' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Số mô hình' AS spec_key, '3 (rồng, cá, phượng hoàng)' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 47520 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [08] Mô hình lắp ráp Gundam RX-78-2  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-ky-thuat');
SET @brand = (SELECT id FROM brands WHERE slug = 'bandai');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Mô hình lắp ráp Gundam RX-78-2', 'mo-hinh-lap-rap-gundam-rx-78-2', 'Đồ chơi Bandai chính hãng, an toàn cho bé', 'Mô hình lắp ráp Gundam RX-78-2 chính hãng Bandai, lắp khớp không cần keo. Chọn phiên bản HG (dễ lắp), RG (chi tiết cao) hoặc MG (khung xương trong).', @cat, @brand, 96, NULL, 'UNISEX', 'Nhựa PS', 'Nhật Bản', 'Nhật Bản', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Phiên bản', NULL, 'ACTIVE', TRUE, FALSE, 390000, 1590000, 31, 32, 0, 0, 'mo hinh lap rap gundam rx-78-2 bandai gundam ban-0047-01 ban-0047-02 ban-0047-03', 'Mô hình lắp ráp Gundam RX-78-2 | ToyLand', 'Đồ chơi Bandai chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 37440 MINUTE), DATE_SUB(NOW(), INTERVAL 37440 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'mo-hinh-lap-rap-gundam-rx-78-2');
SET @p = (SELECT id FROM products WHERE slug = 'mo-hinh-lap-rap-gundam-rx-78-2');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'BAN-0047-01' AS sku, '8930000000064' AS barcode, 'HG 1/144' AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 20 AS stock_quantity, 300 AS weight_gram, 'https://placehold.co/600x600/607D8B/3E2723/png?text=HG+1144' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'BAN-0047-02' AS sku, '8930000000065' AS barcode, 'RG 1/144' AS option1, NULL AS option2, 890000 AS price, NULL AS compare_at_price, 534000 AS cost_price, 8 AS stock_quantity, 450 AS weight_gram, 'https://placehold.co/600x600/607D8B/3E2723/png?text=RG+1144' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'BAN-0047-03' AS sku, '8930000000066' AS barcode, 'MG 1/100' AS option1, NULL AS option2, 1590000 AS price, 1790000 AS compare_at_price, 954000 AS cost_price, 3 AS stock_quantity, 900 AS weight_gram, 'https://placehold.co/600x600/607D8B/3E2723/png?text=MG+1100' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/607D8B/3E2723/png?text=M+hnh+lp+rp+Gundam+R' AS url, 'Mô hình lắp ráp Gundam RX-78-2' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=M+hnh+lp+rp+Gundam+R+2' AS url, 'Mô hình lắp ráp Gundam RX-78-2' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=M+hnh+lp+rp+Gundam+R+3' AS url, 'Mô hình lắp ráp Gundam RX-78-2' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=M+hnh+lp+rp+Gundam+R+4' AS url, 'Mô hình lắp ráp Gundam RX-78-2' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Cần keo dán' AS spec_key, 'Không (lắp khớp)' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chiều cao' AS spec_key, '13 - 18 cm tuỳ phiên bản' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'gundam';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 37440 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [09] LEGO Speed Champions Ferrari F40 76934
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-ky-thuat');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Speed Champions Ferrari F40 76934', 'lego-speed-champions-ferrari-f40-76934', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Speed Champions Ferrari F40 76934 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 108, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 599000, 599000, 14, 101, 0, 0, 'lego speed champions ferrari f40 76934 lego leg-0048', 'LEGO Speed Champions Ferrari F40 76934 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 7200 MINUTE), DATE_SUB(NOW(), INTERVAL 7200 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-speed-champions-ferrari-f40-76934');
SET @p = (SELECT id FROM products WHERE slug = 'lego-speed-champions-ferrari-f40-76934');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0048' AS sku, '8930000000067' AS barcode, NULL AS option1, NULL AS option2, 599000 AS price, NULL AS compare_at_price, 359000 AS cost_price, 14 AS stock_quantity, 420 AS weight_gram, 'https://placehold.co/600x600/C62828/3E2723/png?text=LEGO+Speed+Champions+F' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/C62828/3E2723/png?text=LEGO+Speed+Champions+Fer' AS url, 'LEGO Speed Champions Ferrari F40 76934' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Speed+Champions+Fer+2' AS url, 'LEGO Speed Champions Ferrari F40 76934' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Speed+Champions+Fer+3' AS url, 'LEGO Speed Champions Ferrari F40 76934' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Speed+Champions+Fer+4' AS url, 'LEGO Speed Champions Ferrari F40 76934' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '318' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chủ đề' AS spec_key, 'LEGO Speed Champions' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 7200 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [10] LEGO Marvel Chiến giáp Hulkbuster 76247
SET @cat = (SELECT id FROM categories WHERE slug = 'lap-rap-ky-thuat');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Marvel Chiến giáp Hulkbuster 76247', 'lego-marvel-chien-giap-hulkbuster-76247', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Marvel Chiến giáp Hulkbuster 76247 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 999000, 999000, 0, 0, 0, 0, 'lego marvel chien giap hulkbuster 76247 lego marvel avengers leg-0049', 'LEGO Marvel Chiến giáp Hulkbuster 76247 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 136800 MINUTE), DATE_SUB(NOW(), INTERVAL 136800 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-marvel-chien-giap-hulkbuster-76247');
SET @p = (SELECT id FROM products WHERE slug = 'lego-marvel-chien-giap-hulkbuster-76247');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0049' AS sku, '8930000000068' AS barcode, NULL AS option1, NULL AS option2, 999000 AS price, NULL AS compare_at_price, 599000 AS cost_price, 0 AS stock_quantity, 650 AS weight_gram, 'https://placehold.co/600x600/B71C1C/3E2723/png?text=LEGO+Marvel+Chin+gip' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/B71C1C/3E2723/png?text=LEGO+Marvel+Chin+gip+H' AS url, 'LEGO Marvel Chiến giáp Hulkbuster 76247' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Marvel+Chin+gip+H+2' AS url, 'LEGO Marvel Chiến giáp Hulkbuster 76247' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Marvel+Chin+gip+H+3' AS url, 'LEGO Marvel Chiến giáp Hulkbuster 76247' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Marvel+Chin+gip+H+4' AS url, 'LEGO Marvel Chiến giáp Hulkbuster 76247' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '385' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chủ đề' AS spec_key, 'LEGO Marvel' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'marvel-avengers';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 136800 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [11] LEGO Duplo Tàu hỏa số đếm 10954
SET @cat = (SELECT id FROM categories WHERE slug = 'gach-lon-cho-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Duplo Tàu hỏa số đếm 10954', 'lego-duplo-tau-hoa-so-dem-10954', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Duplo Tàu hỏa số đếm 10954 chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 18, 60, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 699000, 699000, 18, 157, 0, 0, 'lego duplo tau hoa so dem 10954 lego leg-0050', 'LEGO Duplo Tàu hỏa số đếm 10954 | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 57600 MINUTE), DATE_SUB(NOW(), INTERVAL 57600 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-duplo-tau-hoa-so-dem-10954');
SET @p = (SELECT id FROM products WHERE slug = 'lego-duplo-tau-hoa-so-dem-10954');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0050' AS sku, '8930000000069' AS barcode, NULL AS option1, NULL AS option2, 699000 AS price, NULL AS compare_at_price, 419000 AS cost_price, 18 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/FFD54F/3E2723/png?text=LEGO+Duplo+Tu+ha+s' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FFD54F/3E2723/png?text=LEGO+Duplo+Tu+ha+s' AS url, 'LEGO Duplo Tàu hỏa số đếm 10954' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Duplo+Tu+ha+s++2' AS url, 'LEGO Duplo Tàu hỏa số đếm 10954' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Duplo+Tu+ha+s++3' AS url, 'LEGO Duplo Tàu hỏa số đếm 10954' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Duplo+Tu+ha+s++4' AS url, 'LEGO Duplo Tàu hỏa số đếm 10954' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số mảnh ghép' AS spec_key, '23' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kỹ năng' AS spec_key, 'Đếm số 1 - 10' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 57600 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [12] LEGO Duplo Thùng gạch sáng tạo  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'gach-lon-cho-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'lego');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'LEGO Duplo Thùng gạch sáng tạo', 'lego-duplo-thung-gach-sang-tao', 'Đồ chơi LEGO chính hãng, an toàn cho bé', 'LEGO Duplo Thùng gạch sáng tạo chính hãng LEGO. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 18, 60, 'UNISEX', 'Nhựa ABS', 'Đan Mạch', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty TNHH LEGO Việt Nam (nhà phân phối chính thức)', 30, 20, 10, 'Kích thước bộ', NULL, 'ACTIVE', FALSE, FALSE, 649000, 1290000, 29, 22, 0, 0, 'lego duplo thung gach sang tao lego leg-0051-01 leg-0051-02 leg-0051-03', 'LEGO Duplo Thùng gạch sáng tạo | ToyLand', 'Đồ chơi LEGO chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 86400 MINUTE), DATE_SUB(NOW(), INTERVAL 86400 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'lego-duplo-thung-gach-sang-tao');
SET @p = (SELECT id FROM products WHERE slug = 'lego-duplo-thung-gach-sang-tao');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'LEG-0051-01' AS sku, '8930000000070' AS barcode, '65 chi tiết' AS option1, NULL AS option2, 649000 AS price, NULL AS compare_at_price, 389000 AS cost_price, 15 AS stock_quantity, 900 AS weight_gram, 'https://placehold.co/600x600/FFCA28/3E2723/png?text=65+chi+tit' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'LEG-0051-02' AS sku, '8930000000071' AS barcode, '85 chi tiết' AS option1, NULL AS option2, 899000 AS price, 999000 AS compare_at_price, 539000 AS cost_price, 10 AS stock_quantity, 1200 AS weight_gram, 'https://placehold.co/600x600/FFCA28/3E2723/png?text=85+chi+tit' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'LEG-0051-03' AS sku, '8930000000072' AS barcode, '120 chi tiết' AS option1, NULL AS option2, 1290000 AS price, NULL AS compare_at_price, 774000 AS cost_price, 4 AS stock_quantity, 1700 AS weight_gram, 'https://placehold.co/600x600/FFCA28/3E2723/png?text=120+chi+tit' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FFCA28/3E2723/png?text=LEGO+Duplo+Thng+gch+s' AS url, 'LEGO Duplo Thùng gạch sáng tạo' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=LEGO+Duplo+Thng+gch+s+2' AS url, 'LEGO Duplo Thùng gạch sáng tạo' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=LEGO+Duplo+Thng+gch+s+3' AS url, 'LEGO Duplo Thùng gạch sáng tạo' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=LEGO+Duplo+Thng+gch+s+4' AS url, 'LEGO Duplo Thùng gạch sáng tạo' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Chất liệu' AS spec_key, 'Nhựa ABS an toàn cho bé' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kích thước gạch' AS spec_key, 'Gấp đôi gạch LEGO thường' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 86400 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [13] Fisher-Price Tháp xếp chồng cầu vồng
SET @cat = (SELECT id FROM categories WHERE slug = 'gach-lon-cho-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'fisher-price');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Fisher-Price Tháp xếp chồng cầu vồng', 'fisher-price-thap-xep-chong-cau-vong', 'Đồ chơi Fisher-Price chính hãng, an toàn cho bé', 'Fisher-Price Tháp xếp chồng cầu vồng chính hãng Fisher-Price. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 6, 36, 'UNISEX', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 249000, 249000, 40, 137, 0, 0, 'fisher-price thap xep chong cau vong fisher-price fis-0052', 'Fisher-Price Tháp xếp chồng cầu vồng | ToyLand', 'Đồ chơi Fisher-Price chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 79200 MINUTE), DATE_SUB(NOW(), INTERVAL 79200 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'fisher-price-thap-xep-chong-cau-vong');
SET @p = (SELECT id FROM products WHERE slug = 'fisher-price-thap-xep-chong-cau-vong');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'FIS-0052' AS sku, '8930000000073' AS barcode, NULL AS option1, NULL AS option2, 249000 AS price, NULL AS compare_at_price, 149000 AS cost_price, 40 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/FF8A65/3E2723/png?text=FisherPrice+Thp+xp' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FF8A65/3E2723/png?text=FisherPrice+Thp+xp+ch' AS url, 'Fisher-Price Tháp xếp chồng cầu vồng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=FisherPrice+Thp+xp+ch+2' AS url, 'Fisher-Price Tháp xếp chồng cầu vồng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=FisherPrice+Thp+xp+ch+3' AS url, 'Fisher-Price Tháp xếp chồng cầu vồng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=FisherPrice+Thp+xp+ch+4' AS url, 'Fisher-Price Tháp xếp chồng cầu vồng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số vòng' AS spec_key, '5' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kỹ năng' AS spec_key, 'Nhận biết màu sắc, kích thước' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 79200 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [14] Bộ gạch nam châm 3D Mideer  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'gach-lon-cho-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'mideer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ gạch nam châm 3D Mideer', 'bo-gach-nam-cham-3d-mideer', 'Đồ chơi Mideer chính hãng, an toàn cho bé', 'Bộ gạch nam châm 3D Mideer chính hãng Mideer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS & nam châm', 'Trung Quốc', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Số miếng', NULL, 'ACTIVE', TRUE, FALSE, 390000, 990000, 39, 62, 0, 0, 'bo gach nam cham 3d mideer mideer mid-0053-01 mid-0053-02 mid-0053-03', 'Bộ gạch nam châm 3D Mideer | ToyLand', 'Đồ chơi Mideer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 25920 MINUTE), DATE_SUB(NOW(), INTERVAL 25920 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-gach-nam-cham-3d-mideer');
SET @p = (SELECT id FROM products WHERE slug = 'bo-gach-nam-cham-3d-mideer');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MID-0053-01' AS sku, '8930000000074' AS barcode, '32 miếng' AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 25 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/4FC3F7/3E2723/png?text=32+ming' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'MID-0053-02' AS sku, '8930000000075' AS barcode, '64 miếng' AS option1, NULL AS option2, 690000 AS price, 790000 AS compare_at_price, 414000 AS cost_price, 12 AS stock_quantity, 1300 AS weight_gram, 'https://placehold.co/600x600/4FC3F7/3E2723/png?text=64+ming' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'MID-0053-03' AS sku, '8930000000076' AS barcode, '100 miếng' AS option1, NULL AS option2, 990000 AS price, 1190000 AS compare_at_price, 594000 AS cost_price, 2 AS stock_quantity, 2000 AS weight_gram, 'https://placehold.co/600x600/4FC3F7/3E2723/png?text=100+ming' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/4FC3F7/3E2723/png?text=B+gch+nam+chm+3D+Mide' AS url, 'Bộ gạch nam châm 3D Mideer' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+gch+nam+chm+3D+Mide+2' AS url, 'Bộ gạch nam châm 3D Mideer' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+gch+nam+chm+3D+Mide+3' AS url, 'Bộ gạch nam châm 3D Mideer' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+gch+nam+chm+3D+Mide+4' AS url, 'Bộ gạch nam châm 3D Mideer' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Nam châm' AS spec_key, 'Bọc kín, không rơi ra' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kết nối' AS spec_key, 'Lắp ghép hình 2D & 3D' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 25920 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [15] Búp bê Barbie Fashionistas  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'bup-be-thoi-trang');
SET @brand = (SELECT id FROM brands WHERE slug = 'barbie');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Búp bê Barbie Fashionistas', 'bup-be-barbie-fashionistas', 'Đồ chơi Barbie chính hãng, an toàn cho bé', 'Búp bê Barbie Fashionistas chính hãng Barbie. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'GIRL', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Mẫu', NULL, 'ACTIVE', FALSE, FALSE, 269000, 299000, 35, 17, 0, 0, 'bup be barbie fashionistas barbie bar-0054-01 bar-0054-02 bar-0054-03 bar-0054-04', 'Búp bê Barbie Fashionistas | ToyLand', 'Đồ chơi Barbie chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 43200 MINUTE), DATE_SUB(NOW(), INTERVAL 43200 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bup-be-barbie-fashionistas');
SET @p = (SELECT id FROM products WHERE slug = 'bup-be-barbie-fashionistas');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'BAR-0054-01' AS sku, '8930000000077' AS barcode, 'Váy hoa nhí' AS option1, NULL AS option2, 269000 AS price, NULL AS compare_at_price, 161000 AS cost_price, 18 AS stock_quantity, 300 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=Vy+hoa+nh' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'BAR-0054-02' AS sku, '8930000000078' AS barcode, 'Áo khoác denim' AS option1, NULL AS option2, 269000 AS price, NULL AS compare_at_price, 161000 AS cost_price, 11 AS stock_quantity, 300 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=o+khoc+denim' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'BAR-0054-03' AS sku, '8930000000079' AS barcode, 'Đầm dạ hội' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 6 AS stock_quantity, 320 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=m+d+hi' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'BAR-0054-04' AS sku, '8930000000080' AS barcode, 'Đồ thể thao' AS option1, NULL AS option2, 269000 AS price, NULL AS compare_at_price, 161000 AS cost_price, 0 AS stock_quantity, 300 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=th+thao' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/EC407A/3E2723/png?text=Bp+b+Barbie+Fashionist' AS url, 'Búp bê Barbie Fashionistas' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Bp+b+Barbie+Fashionist+2' AS url, 'Búp bê Barbie Fashionistas' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Bp+b+Barbie+Fashionist+3' AS url, 'Búp bê Barbie Fashionistas' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Bp+b+Barbie+Fashionist+4' AS url, 'Búp bê Barbie Fashionistas' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Chiều cao' AS spec_key, '29 cm' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Khớp' AS spec_key, 'Tay chân cử động' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 43200 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [16] Búp bê Barbie Nàng tiên cá phát sáng
SET @cat = (SELECT id FROM categories WHERE slug = 'bup-be-thoi-trang');
SET @brand = (SELECT id FROM brands WHERE slug = 'barbie');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Búp bê Barbie Nàng tiên cá phát sáng', 'bup-be-barbie-nang-tien-ca-phat-sang', 'Đồ chơi Barbie chính hãng, an toàn cho bé', 'Búp bê Barbie Nàng tiên cá phát sáng chính hãng Barbie. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'GIRL', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, '2 pin LR44 (kèm theo)', TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 529000, 529000, 12, 30, 0, 0, 'bup be barbie nang tien ca phat sang barbie bar-0055', 'Búp bê Barbie Nàng tiên cá phát sáng | ToyLand', 'Đồ chơi Barbie chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 20160 MINUTE), DATE_SUB(NOW(), INTERVAL 20160 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bup-be-barbie-nang-tien-ca-phat-sang');
SET @p = (SELECT id FROM products WHERE slug = 'bup-be-barbie-nang-tien-ca-phat-sang');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'BAR-0055' AS sku, '8930000000081' AS barcode, NULL AS option1, NULL AS option2, 529000 AS price, NULL AS compare_at_price, 317000 AS cost_price, 12 AS stock_quantity, 420 AS weight_gram, 'https://placehold.co/600x600/26C6DA/3E2723/png?text=Bp+b+Barbie+Nng+ti' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/26C6DA/3E2723/png?text=Bp+b+Barbie+Nng+tin' AS url, 'Búp bê Barbie Nàng tiên cá phát sáng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Bp+b+Barbie+Nng+tin++2' AS url, 'Búp bê Barbie Nàng tiên cá phát sáng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Bp+b+Barbie+Nng+tin++3' AS url, 'Búp bê Barbie Nàng tiên cá phát sáng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Bp+b+Barbie+Nng+tin++4' AS url, 'Búp bê Barbie Nàng tiên cá phát sáng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Hiệu ứng' AS spec_key, 'Đuôi phát sáng khi xuống nước' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 20160 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [17] Búp bê công chúa Disney cổ điển  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'bup-be-thoi-trang');
SET @brand = (SELECT id FROM brands WHERE slug = 'barbie');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Búp bê công chúa Disney cổ điển', 'bup-be-cong-chua-disney-co-dien', 'Đồ chơi Barbie chính hãng, an toàn cho bé', 'Búp bê công chúa Disney cổ điển chính hãng Barbie. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'GIRL', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Nhân vật', NULL, 'ACTIVE', TRUE, FALSE, 390000, 390000, 28, 119, 0, 0, 'bup be cong chua disney co dien barbie disney princess bar-0056-01 bar-0056-02 bar-0056-03 bar-0056-04', 'Búp bê công chúa Disney cổ điển | ToyLand', 'Đồ chơi Barbie chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 54720 MINUTE), DATE_SUB(NOW(), INTERVAL 54720 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bup-be-cong-chua-disney-co-dien');
SET @p = (SELECT id FROM products WHERE slug = 'bup-be-cong-chua-disney-co-dien');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'BAR-0056-01' AS sku, '8930000000082' AS barcode, 'Bạch Tuyết' AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 9 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/BA68C8/3E2723/png?text=Bch+Tuyt' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'BAR-0056-02' AS sku, '8930000000083' AS barcode, 'Lọ Lem' AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 14 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/BA68C8/3E2723/png?text=L+Lem' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'BAR-0056-03' AS sku, '8930000000084' AS barcode, 'Ariel' AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 5 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/BA68C8/3E2723/png?text=Ariel' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'BAR-0056-04' AS sku, '8930000000085' AS barcode, 'Belle' AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 0 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/BA68C8/3E2723/png?text=Belle' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/BA68C8/3E2723/png?text=Bp+b+cng+cha+Disney' AS url, 'Búp bê công chúa Disney cổ điển' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Bp+b+cng+cha+Disney++2' AS url, 'Búp bê công chúa Disney cổ điển' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Bp+b+cng+cha+Disney++3' AS url, 'Búp bê công chúa Disney cổ điển' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Bp+b+cng+cha+Disney++4' AS url, 'Búp bê công chúa Disney cổ điển' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'disney-princess';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 54720 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [18] Búp bê em bé Baby Alive tập ăn  (2 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'bup-be-thoi-trang');
SET @brand = (SELECT id FROM brands WHERE slug = 'hasbro-gaming');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Búp bê em bé Baby Alive tập ăn', 'bup-be-em-be-baby-alive-tap-an', 'Đồ chơi Hasbro Gaming chính hãng, an toàn cho bé', 'Búp bê em bé Baby Alive tập ăn chính hãng Hasbro Gaming. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'GIRL', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Màu tóc', NULL, 'ACTIVE', FALSE, FALSE, 690000, 690000, 14, 115, 0, 0, 'bup be em be baby alive tap an hasbro gaming has-0057-01 has-0057-02', 'Búp bê em bé Baby Alive tập ăn | ToyLand', 'Đồ chơi Hasbro Gaming chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 95040 MINUTE), DATE_SUB(NOW(), INTERVAL 95040 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bup-be-em-be-baby-alive-tap-an');
SET @p = (SELECT id FROM products WHERE slug = 'bup-be-em-be-baby-alive-tap-an');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'HAS-0057-01' AS sku, '8930000000086' AS barcode, 'Tóc vàng' AS option1, NULL AS option2, 690000 AS price, 790000 AS compare_at_price, 414000 AS cost_price, 8 AS stock_quantity, 600 AS weight_gram, 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=Tc+vng' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'HAS-0057-02' AS sku, '8930000000087' AS barcode, 'Tóc nâu' AS option1, NULL AS option2, 690000 AS price, 790000 AS compare_at_price, 414000 AS cost_price, 6 AS stock_quantity, 600 AS weight_gram, 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=Tc+nu' AS image_url, FALSE AS is_default, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=Bp+b+em+b+Baby+Alive' AS url, 'Búp bê em bé Baby Alive tập ăn' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Bp+b+em+b+Baby+Alive++2' AS url, 'Búp bê em bé Baby Alive tập ăn' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Bp+b+em+b+Baby+Alive++3' AS url, 'Búp bê em bé Baby Alive tập ăn' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Bp+b+em+b+Baby+Alive++4' AS url, 'Búp bê em bé Baby Alive tập ăn' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Phụ kiện' AS spec_key, 'Bình sữa, thìa, tã, gói thức ăn' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Chiều cao' AS spec_key, '33 cm' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 95040 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [19] Bộ trang phục Barbie 5 bộ  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'bup-be-thoi-trang');
SET @brand = (SELECT id FROM brands WHERE slug = 'barbie');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ trang phục Barbie 5 bộ', 'bo-trang-phuc-barbie-5-bo', 'Đồ chơi Barbie chính hãng, an toàn cho bé', 'Bộ trang phục Barbie 5 bộ chính hãng Barbie. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'GIRL', 'Vải & nhựa', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Phong cách', NULL, 'ACTIVE', FALSE, FALSE, 199000, 219000, 39, 25, 0, 0, 'bo trang phuc barbie 5 bo barbie bar-0058-01 bar-0058-02 bar-0058-03', 'Bộ trang phục Barbie 5 bộ | ToyLand', 'Đồ chơi Barbie chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 146880 MINUTE), DATE_SUB(NOW(), INTERVAL 146880 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-trang-phuc-barbie-5-bo');
SET @p = (SELECT id FROM products WHERE slug = 'bo-trang-phuc-barbie-5-bo');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'BAR-0058-01' AS sku, '8930000000088' AS barcode, 'Dạo phố' AS option1, NULL AS option2, 199000 AS price, NULL AS compare_at_price, 119000 AS cost_price, 20 AS stock_quantity, 150 AS weight_gram, 'https://placehold.co/600x600/F06292/3E2723/png?text=Do+ph' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'BAR-0058-02' AS sku, '8930000000089' AS barcode, 'Dạ tiệc' AS option1, NULL AS option2, 219000 AS price, NULL AS compare_at_price, 131000 AS cost_price, 12 AS stock_quantity, 150 AS weight_gram, 'https://placehold.co/600x600/F06292/3E2723/png?text=D+tic' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'BAR-0058-03' AS sku, '8930000000090' AS barcode, 'Thể thao' AS option1, NULL AS option2, 199000 AS price, NULL AS compare_at_price, 119000 AS cost_price, 7 AS stock_quantity, 150 AS weight_gram, 'https://placehold.co/600x600/F06292/3E2723/png?text=Th+thao' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F06292/3E2723/png?text=B+trang+phc+Barbie+5+b' AS url, 'Bộ trang phục Barbie 5 bộ' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+trang+phc+Barbie+5+b+2' AS url, 'Bộ trang phục Barbie 5 bộ' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+trang+phc+Barbie+5+b+3' AS url, 'Bộ trang phục Barbie 5 bộ' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+trang+phc+Barbie+5+b+4' AS url, 'Bộ trang phục Barbie 5 bộ' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 146880 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [20] Sylvanian Families Ngôi nhà gỗ ấm cúng
SET @cat = (SELECT id FROM categories WHERE slug = 'nha-bup-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'sylvanian-families');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Sylvanian Families Ngôi nhà gỗ ấm cúng', 'sylvanian-families-ngoi-nha-go-am-cung', 'Đồ chơi Sylvanian Families chính hãng, an toàn cho bé', 'Sylvanian Families Ngôi nhà gỗ ấm cúng chính hãng Sylvanian Families. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS', 'Nhật Bản', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 40, 30, 30, NULL, NULL, 'ACTIVE', TRUE, FALSE, 1890000, 1890000, 5, 69, 0, 0, 'sylvanian families ngoi nha go am cung sylvanian families syl-0059', 'Sylvanian Families Ngôi nhà gỗ ấm cúng | ToyLand', 'Đồ chơi Sylvanian Families chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 63360 MINUTE), DATE_SUB(NOW(), INTERVAL 63360 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'sylvanian-families-ngoi-nha-go-am-cung');
SET @p = (SELECT id FROM products WHERE slug = 'sylvanian-families-ngoi-nha-go-am-cung');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'SYL-0059' AS sku, '8930000000091' AS barcode, NULL AS option1, NULL AS option2, 1890000 AS price, 2190000 AS compare_at_price, 1134000 AS cost_price, 5 AS stock_quantity, 1800 AS weight_gram, 'https://placehold.co/600x600/A1887F/3E2723/png?text=Sylvanian+Families+Ng' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/A1887F/3E2723/png?text=Sylvanian+Families+Ngi' AS url, 'Sylvanian Families Ngôi nhà gỗ ấm cúng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Sylvanian+Families+Ngi++2' AS url, 'Sylvanian Families Ngôi nhà gỗ ấm cúng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Sylvanian+Families+Ngi++3' AS url, 'Sylvanian Families Ngôi nhà gỗ ấm cúng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Sylvanian+Families+Ngi++4' AS url, 'Sylvanian Families Ngôi nhà gỗ ấm cúng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số phòng' AS spec_key, '3' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Mái nhà' AS spec_key, 'Mở được để chơi' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 63360 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [21] Sylvanian Families Gia đình thỏ Chocolate  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'nha-bup-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'sylvanian-families');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Sylvanian Families Gia đình thỏ Chocolate', 'sylvanian-families-gia-dinh-tho-chocolate', 'Đồ chơi Sylvanian Families chính hãng, an toàn cho bé', 'Sylvanian Families Gia đình thỏ Chocolate chính hãng Sylvanian Families. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa & lông nhung', 'Nhật Bản', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Bộ', NULL, 'ACTIVE', FALSE, FALSE, 190000, 690000, 51, 31, 0, 0, 'sylvanian families gia dinh tho chocolate sylvanian families syl-0060-01 syl-0060-02 syl-0060-03', 'Sylvanian Families Gia đình thỏ Chocolate | ToyLand', 'Đồ chơi Sylvanian Families chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 74880 MINUTE), DATE_SUB(NOW(), INTERVAL 74880 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'sylvanian-families-gia-dinh-tho-chocolate');
SET @p = (SELECT id FROM products WHERE slug = 'sylvanian-families-gia-dinh-tho-chocolate');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'SYL-0060-01' AS sku, '8930000000092' AS barcode, 'Gia đình 4 thành viên' AS option1, NULL AS option2, 690000 AS price, NULL AS compare_at_price, 414000 AS cost_price, 10 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/8D6E63/3E2723/png?text=Gia+nh+4+thnh+vin' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'SYL-0060-02' AS sku, '8930000000093' AS barcode, 'Cặp anh em' AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 16 AS stock_quantity, 200 AS weight_gram, 'https://placehold.co/600x600/8D6E63/3E2723/png?text=Cp+anh+em' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'SYL-0060-03' AS sku, '8930000000094' AS barcode, 'Em bé' AS option1, NULL AS option2, 190000 AS price, NULL AS compare_at_price, 114000 AS cost_price, 25 AS stock_quantity, 100 AS weight_gram, 'https://placehold.co/600x600/8D6E63/3E2723/png?text=Em+b' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/8D6E63/3E2723/png?text=Sylvanian+Families+Gia' AS url, 'Sylvanian Families Gia đình thỏ Chocolate' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Sylvanian+Families+Gia++2' AS url, 'Sylvanian Families Gia đình thỏ Chocolate' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Sylvanian+Families+Gia++3' AS url, 'Sylvanian Families Gia đình thỏ Chocolate' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Sylvanian+Families+Gia++4' AS url, 'Sylvanian Families Gia đình thỏ Chocolate' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 74880 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [22] Barbie Tiệm bánh ngọt
SET @cat = (SELECT id FROM categories WHERE slug = 'nha-bup-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'barbie');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Barbie Tiệm bánh ngọt', 'barbie-tiem-banh-ngot', 'Đồ chơi Barbie chính hãng, an toàn cho bé', 'Barbie Tiệm bánh ngọt chính hãng Barbie. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'GIRL', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 890000, 890000, 9, 149, 0, 0, 'barbie tiem banh ngot barbie bar-0061', 'Barbie Tiệm bánh ngọt | ToyLand', 'Đồ chơi Barbie chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 109440 MINUTE), DATE_SUB(NOW(), INTERVAL 109440 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'barbie-tiem-banh-ngot');
SET @p = (SELECT id FROM products WHERE slug = 'barbie-tiem-banh-ngot');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'BAR-0061' AS sku, '8930000000095' AS barcode, NULL AS option1, NULL AS option2, 890000 AS price, NULL AS compare_at_price, 534000 AS cost_price, 9 AS stock_quantity, 1100 AS weight_gram, 'https://placehold.co/600x600/F48FB1/3E2723/png?text=Barbie+Tim+bnh+ngt' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F48FB1/3E2723/png?text=Barbie+Tim+bnh+ngt' AS url, 'Barbie Tiệm bánh ngọt' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Barbie+Tim+bnh+ngt+2' AS url, 'Barbie Tiệm bánh ngọt' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Barbie+Tim+bnh+ngt+3' AS url, 'Barbie Tiệm bánh ngọt' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Barbie+Tim+bnh+ngt+4' AS url, 'Barbie Tiệm bánh ngọt' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Phụ kiện' AS spec_key, 'Hơn 20 món' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kèm búp bê' AS spec_key, '1' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 109440 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [23] Sylvanian Families Nhà trẻ cho em bé
SET @cat = (SELECT id FROM categories WHERE slug = 'nha-bup-be');
SET @brand = (SELECT id FROM brands WHERE slug = 'sylvanian-families');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Sylvanian Families Nhà trẻ cho em bé', 'sylvanian-families-nha-tre-cho-em-be', 'Đồ chơi Sylvanian Families chính hãng, an toàn cho bé', 'Sylvanian Families Nhà trẻ cho em bé chính hãng Sylvanian Families. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS', 'Nhật Bản', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'COMING_SOON', FALSE, FALSE, 1290000, 1290000, 0, 0, 0, 0, 'sylvanian families nha tre cho em be sylvanian families syl-0062', 'Sylvanian Families Nhà trẻ cho em bé | ToyLand', 'Đồ chơi Sylvanian Families chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 4320 MINUTE), DATE_SUB(NOW(), INTERVAL 4320 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'sylvanian-families-nha-tre-cho-em-be');
SET @p = (SELECT id FROM products WHERE slug = 'sylvanian-families-nha-tre-cho-em-be');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'SYL-0062' AS sku, '8930000000096' AS barcode, NULL AS option1, NULL AS option2, 1290000 AS price, NULL AS compare_at_price, 774000 AS cost_price, 0 AS stock_quantity, 1200 AS weight_gram, 'https://placehold.co/600x600/BCAAA4/3E2723/png?text=Sylvanian+Families+Nh' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/BCAAA4/3E2723/png?text=Sylvanian+Families+Nh+t' AS url, 'Sylvanian Families Nhà trẻ cho em bé' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Sylvanian+Families+Nh+t+2' AS url, 'Sylvanian Families Nhà trẻ cho em bé' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Sylvanian+Families+Nh+t+3' AS url, 'Sylvanian Families Nhà trẻ cho em bé' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Sylvanian+Families+Nh+t+4' AS url, 'Sylvanian Families Nhà trẻ cho em bé' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 4320 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [24] Xe mô hình Tomica  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-mo-hinh');
SET @brand = (SELECT id FROM brands WHERE slug = 'tomica');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Xe mô hình Tomica', 'xe-mo-hinh-tomica', 'Đồ chơi Tomica chính hãng, an toàn cho bé', 'Xe mô hình Tomica chính hãng Tomica. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Kim loại & nhựa', 'Nhật Bản', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Mẫu xe', NULL, 'ACTIVE', TRUE, FALSE, 129000, 139000, 61, 116, 0, 0, 'xe mo hinh tomica tomica tom-0063-01 tom-0063-02 tom-0063-03 tom-0063-04', 'Xe mô hình Tomica | ToyLand', 'Đồ chơi Tomica chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 34560 MINUTE), DATE_SUB(NOW(), INTERVAL 34560 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'xe-mo-hinh-tomica');
SET @p = (SELECT id FROM products WHERE slug = 'xe-mo-hinh-tomica');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'TOM-0063-01' AS sku, '8930000000097' AS barcode, 'Toyota Supra' AS option1, NULL AS option2, 129000 AS price, NULL AS compare_at_price, 77000 AS cost_price, 35 AS stock_quantity, 90 AS weight_gram, 'https://placehold.co/600x600/E53935/3E2723/png?text=Toyota+Supra' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'TOM-0063-02' AS sku, '8930000000098' AS barcode, 'Honda Civic Type R' AS option1, NULL AS option2, 129000 AS price, NULL AS compare_at_price, 77000 AS cost_price, 22 AS stock_quantity, 90 AS weight_gram, 'https://placehold.co/600x600/E53935/3E2723/png?text=Honda+Civic+Type+R' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'TOM-0063-03' AS sku, '8930000000099' AS barcode, 'Nissan GT-R' AS option1, NULL AS option2, 139000 AS price, NULL AS compare_at_price, 83000 AS cost_price, 4 AS stock_quantity, 90 AS weight_gram, 'https://placehold.co/600x600/E53935/3E2723/png?text=Nissan+GTR' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'TOM-0063-04' AS sku, '8930000000100' AS barcode, 'Mazda RX-7' AS option1, NULL AS option2, 129000 AS price, NULL AS compare_at_price, 77000 AS cost_price, 0 AS stock_quantity, 90 AS weight_gram, 'https://placehold.co/600x600/E53935/3E2723/png?text=Mazda+RX7' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/E53935/3E2723/png?text=Xe+m+hnh+Tomica' AS url, 'Xe mô hình Tomica' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Xe+m+hnh+Tomica+2' AS url, 'Xe mô hình Tomica' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Xe+m+hnh+Tomica+3' AS url, 'Xe mô hình Tomica' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Xe+m+hnh+Tomica+4' AS url, 'Xe mô hình Tomica' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Tỉ lệ' AS spec_key, '~1:64' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Bánh xe' AS spec_key, 'Có giảm xóc' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 34560 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [25] Tomica Bãi đỗ xe nhiều tầng
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-mo-hinh');
SET @brand = (SELECT id FROM brands WHERE slug = 'tomica');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Tomica Bãi đỗ xe nhiều tầng', 'tomica-bai-do-xe-nhieu-tang', 'Đồ chơi Tomica chính hãng, an toàn cho bé', 'Tomica Bãi đỗ xe nhiều tầng chính hãng Tomica. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS', 'Nhật Bản', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 45, 35, 25, NULL, NULL, 'ACTIVE', FALSE, FALSE, 990000, 990000, 6, 23, 0, 0, 'tomica bai do xe nhieu tang tomica tom-0064', 'Tomica Bãi đỗ xe nhiều tầng | ToyLand', 'Đồ chơi Tomica chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 129600 MINUTE), DATE_SUB(NOW(), INTERVAL 129600 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'tomica-bai-do-xe-nhieu-tang');
SET @p = (SELECT id FROM products WHERE slug = 'tomica-bai-do-xe-nhieu-tang');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'TOM-0064' AS sku, '8930000000101' AS barcode, NULL AS option1, NULL AS option2, 990000 AS price, NULL AS compare_at_price, 594000 AS cost_price, 6 AS stock_quantity, 1600 AS weight_gram, 'https://placehold.co/600x600/1E88E5/3E2723/png?text=Tomica+Bi++xe+nhiu' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/1E88E5/3E2723/png?text=Tomica+Bi++xe+nhiu+t' AS url, 'Tomica Bãi đỗ xe nhiều tầng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Tomica+Bi++xe+nhiu+t+2' AS url, 'Tomica Bãi đỗ xe nhiều tầng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Tomica+Bi++xe+nhiu+t+3' AS url, 'Tomica Bãi đỗ xe nhiều tầng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Tomica+Bi++xe+nhiu+t+4' AS url, 'Tomica Bãi đỗ xe nhiều tầng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số tầng' AS spec_key, '4' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Thang máy' AS spec_key, 'Quay tay' AS spec_value, 1 AS sort_order
  UNION ALL SELECT 'Xe kèm theo' AS spec_key, '1' AS spec_value, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 129600 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [26] Hot Wheels Monster Trucks 1:24  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-mo-hinh');
SET @brand = (SELECT id FROM brands WHERE slug = 'hot-wheels');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Hot Wheels Monster Trucks 1:24', 'hot-wheels-monster-trucks-1-24', 'Đồ chơi Hot Wheels chính hãng, an toàn cho bé', 'Hot Wheels Monster Trucks 1:24 chính hãng Hot Wheels. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'BOY', 'Kim loại & nhựa', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Mẫu xe', NULL, 'ACTIVE', FALSE, FALSE, 299000, 299000, 26, 152, 0, 0, 'hot wheels monster trucks 1:24 hot wheels hot-0065-01 hot-0065-02 hot-0065-03', 'Hot Wheels Monster Trucks 1:24 | ToyLand', 'Đồ chơi Hot Wheels chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 21600 MINUTE), DATE_SUB(NOW(), INTERVAL 21600 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'hot-wheels-monster-trucks-1-24');
SET @p = (SELECT id FROM products WHERE slug = 'hot-wheels-monster-trucks-1-24');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'HOT-0065-01' AS sku, '8930000000102' AS barcode, 'Bone Shaker' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 14 AS stock_quantity, 400 AS weight_gram, 'https://placehold.co/600x600/FB8C00/3E2723/png?text=Bone+Shaker' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'HOT-0065-02' AS sku, '8930000000103' AS barcode, 'Mega-Wrex' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 9 AS stock_quantity, 400 AS weight_gram, 'https://placehold.co/600x600/FB8C00/3E2723/png?text=MegaWrex' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'HOT-0065-03' AS sku, '8930000000104' AS barcode, 'Tiger Shark' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 3 AS stock_quantity, 400 AS weight_gram, 'https://placehold.co/600x600/FB8C00/3E2723/png?text=Tiger+Shark' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FB8C00/3E2723/png?text=Hot+Wheels+Monster+Truck' AS url, 'Hot Wheels Monster Trucks 1:24' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Hot+Wheels+Monster+Truck+2' AS url, 'Hot Wheels Monster Trucks 1:24' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Hot+Wheels+Monster+Truck+3' AS url, 'Hot Wheels Monster Trucks 1:24' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Hot+Wheels+Monster+Truck+4' AS url, 'Hot Wheels Monster Trucks 1:24' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Tỉ lệ' AS spec_key, '1:24' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 21600 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [27] Hot Wheels Xe tải vận chuyển Mega Hauler
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-mo-hinh');
SET @brand = (SELECT id FROM brands WHERE slug = 'hot-wheels');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Hot Wheels Xe tải vận chuyển Mega Hauler', 'hot-wheels-xe-tai-van-chuyen-mega-hauler', 'Đồ chơi Hot Wheels chính hãng, an toàn cho bé', 'Hot Wheels Xe tải vận chuyển Mega Hauler chính hãng Hot Wheels. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'BOY', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 690000, 690000, 11, 39, 0, 0, 'hot wheels xe tai van chuyen mega hauler hot wheels hot-0066', 'Hot Wheels Xe tải vận chuyển Mega Hauler | ToyLand', 'Đồ chơi Hot Wheels chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 83520 MINUTE), DATE_SUB(NOW(), INTERVAL 83520 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'hot-wheels-xe-tai-van-chuyen-mega-hauler');
SET @p = (SELECT id FROM products WHERE slug = 'hot-wheels-xe-tai-van-chuyen-mega-hauler');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'HOT-0066' AS sku, '8930000000105' AS barcode, NULL AS option1, NULL AS option2, 690000 AS price, 790000 AS compare_at_price, 414000 AS cost_price, 11 AS stock_quantity, 1400 AS weight_gram, 'https://placehold.co/600x600/1976D2/3E2723/png?text=Hot+Wheels+Xe+ti+vn' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/1976D2/3E2723/png?text=Hot+Wheels+Xe+ti+vn+ch' AS url, 'Hot Wheels Xe tải vận chuyển Mega Hauler' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Hot+Wheels+Xe+ti+vn+ch+2' AS url, 'Hot Wheels Xe tải vận chuyển Mega Hauler' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Hot+Wheels+Xe+ti+vn+ch+3' AS url, 'Hot Wheels Xe tải vận chuyển Mega Hauler' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Hot+Wheels+Xe+ti+vn+ch+4' AS url, 'Hot Wheels Xe tải vận chuyển Mega Hauler' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Sức chứa' AS spec_key, 'Hơn 50 xe 1:64' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Xe kèm theo' AS spec_key, '4' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 83520 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [28] Xe cứu hỏa Paw Patrol Marshall
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-mo-hinh');
SET @brand = (SELECT id FROM brands WHERE slug = 'hasbro-gaming');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Xe cứu hỏa Paw Patrol Marshall', 'xe-cuu-hoa-paw-patrol-marshall', 'Đồ chơi Hasbro Gaming chính hãng, an toàn cho bé', 'Xe cứu hỏa Paw Patrol Marshall chính hãng Hasbro Gaming. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, '3 pin AAA (kèm theo)', TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 529000, 529000, 13, 65, 0, 0, 'xe cuu hoa paw patrol marshall hasbro gaming paw patrol has-0067', 'Xe cứu hỏa Paw Patrol Marshall | ToyLand', 'Đồ chơi Hasbro Gaming chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 116640 MINUTE), DATE_SUB(NOW(), INTERVAL 116640 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'xe-cuu-hoa-paw-patrol-marshall');
SET @p = (SELECT id FROM products WHERE slug = 'xe-cuu-hoa-paw-patrol-marshall');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'HAS-0067' AS sku, '8930000000106' AS barcode, NULL AS option1, NULL AS option2, 529000 AS price, NULL AS compare_at_price, 317000 AS cost_price, 13 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/E53935/3E2723/png?text=Xe+cu+ha+Paw+Patrol' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/E53935/3E2723/png?text=Xe+cu+ha+Paw+Patrol+Ma' AS url, 'Xe cứu hỏa Paw Patrol Marshall' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Xe+cu+ha+Paw+Patrol+Ma+2' AS url, 'Xe cứu hỏa Paw Patrol Marshall' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Xe+cu+ha+Paw+Patrol+Ma+3' AS url, 'Xe cứu hỏa Paw Patrol Marshall' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Xe+cu+ha+Paw+Patrol+Ma+4' AS url, 'Xe cứu hỏa Paw Patrol Marshall' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Âm thanh & đèn' AS spec_key, 'Có' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'paw-patrol';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 116640 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [29] Xe điều khiển Mercedes-AMG G63 1:14  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-dieu-khien');
SET @brand = (SELECT id FROM brands WHERE slug = 'rastar');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Xe điều khiển Mercedes-AMG G63 1:14', 'xe-dieu-khien-mercedes-amg-g63-1-14', 'Đồ chơi Rastar chính hãng, an toàn cho bé', 'Xe điều khiển Mercedes-AMG G63 1:14 chính hãng Rastar. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'BOY', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, 'Pin sạc 6V (kèm theo)', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Màu sắc', NULL, 'ACTIVE', TRUE, FALSE, 890000, 890000, 12, 169, 0, 0, 'xe dieu khien mercedes-amg g63 1:14 rastar ras-0068-01 ras-0068-02 ras-0068-03', 'Xe điều khiển Mercedes-AMG G63 1:14 | ToyLand', 'Đồ chơi Rastar chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 15840 MINUTE), DATE_SUB(NOW(), INTERVAL 15840 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'xe-dieu-khien-mercedes-amg-g63-1-14');
SET @p = (SELECT id FROM products WHERE slug = 'xe-dieu-khien-mercedes-amg-g63-1-14');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'RAS-0068-01' AS sku, '8930000000107' AS barcode, 'Đen' AS option1, NULL AS option2, 890000 AS price, 1050000 AS compare_at_price, 534000 AS cost_price, 7 AS stock_quantity, 1300 AS weight_gram, 'https://placehold.co/600x600/424242/3E2723/png?text=en' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'RAS-0068-02' AS sku, '8930000000108' AS barcode, 'Trắng' AS option1, NULL AS option2, 890000 AS price, 1050000 AS compare_at_price, 534000 AS cost_price, 5 AS stock_quantity, 1300 AS weight_gram, 'https://placehold.co/600x600/424242/3E2723/png?text=Trng' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'RAS-0068-03' AS sku, '8930000000109' AS barcode, 'Đỏ' AS option1, NULL AS option2, 890000 AS price, 1050000 AS compare_at_price, 534000 AS cost_price, 0 AS stock_quantity, 1300 AS weight_gram, 'https://placehold.co/600x600/424242/3E2723/png?text=' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/424242/3E2723/png?text=Xe+iu+khin+MercedesA' AS url, 'Xe điều khiển Mercedes-AMG G63 1:14' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Xe+iu+khin+MercedesA+2' AS url, 'Xe điều khiển Mercedes-AMG G63 1:14' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Xe+iu+khin+MercedesA+3' AS url, 'Xe điều khiển Mercedes-AMG G63 1:14' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Xe+iu+khin+MercedesA+4' AS url, 'Xe điều khiển Mercedes-AMG G63 1:14' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Tần số' AS spec_key, '2.4 GHz' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Bản quyền' AS spec_key, 'Mercedes-Benz chính hãng' AS spec_value, 1 AS sort_order
  UNION ALL SELECT 'Đèn LED' AS spec_key, 'Có' AS spec_value, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 15840 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [30] Xe điều khiển biến hình robot 2 trong 1  (2 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-dieu-khien');
SET @brand = (SELECT id FROM brands WHERE slug = 'speed-racer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Xe điều khiển biến hình robot 2 trong 1', 'xe-dieu-khien-bien-hinh-robot-2-trong-1', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', 'Xe điều khiển biến hình robot 2 trong 1 chính hãng Speed Racer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'BOY', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, 'Pin sạc Li-ion 3.7V (kèm theo)', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Màu sắc', NULL, 'ACTIVE', FALSE, FALSE, 590000, 590000, 23, 168, 0, 0, 'xe dieu khien bien hinh robot 2 trong 1 speed racer spe-0069-01 spe-0069-02', 'Xe điều khiển biến hình robot 2 trong 1 | ToyLand', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 50400 MINUTE), DATE_SUB(NOW(), INTERVAL 50400 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'xe-dieu-khien-bien-hinh-robot-2-trong-1');
SET @p = (SELECT id FROM products WHERE slug = 'xe-dieu-khien-bien-hinh-robot-2-trong-1');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'SPE-0069-01' AS sku, '8930000000110' AS barcode, 'Đỏ' AS option1, NULL AS option2, 590000 AS price, 690000 AS compare_at_price, 354000 AS cost_price, 13 AS stock_quantity, 900 AS weight_gram, 'https://placehold.co/600x600/EF5350/3E2723/png?text=' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'SPE-0069-02' AS sku, '8930000000111' AS barcode, 'Xanh dương' AS option1, NULL AS option2, 590000 AS price, 690000 AS compare_at_price, 354000 AS cost_price, 10 AS stock_quantity, 900 AS weight_gram, 'https://placehold.co/600x600/EF5350/3E2723/png?text=Xanh+dng' AS image_url, FALSE AS is_default, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/EF5350/3E2723/png?text=Xe+iu+khin+bin+hnh' AS url, 'Xe điều khiển biến hình robot 2 trong 1' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Xe+iu+khin+bin+hnh++2' AS url, 'Xe điều khiển biến hình robot 2 trong 1' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Xe+iu+khin+bin+hnh++3' AS url, 'Xe điều khiển biến hình robot 2 trong 1' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Xe+iu+khin+bin+hnh++4' AS url, 'Xe điều khiển biến hình robot 2 trong 1' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Biến hình' AS spec_key, '1 nút bấm' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Tần số' AS spec_key, '2.4 GHz' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 50400 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [31] Flycam mini có camera cho bé
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-dieu-khien');
SET @brand = (SELECT id FROM brands WHERE slug = 'speed-racer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Flycam mini có camera cho bé', 'flycam-mini-co-camera-cho-be', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', 'Flycam mini có camera cho bé chính hãng Speed Racer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 144, NULL, 'UNISEX', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, 'Pin sạc 3.7V (kèm 2 pin)', FALSE, 'Chỉ bay nơi thoáng, có người lớn giám sát. Tránh xa cánh quạt khi đang quay.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 1290000, 1290000, 4, 157, 0, 0, 'flycam mini co camera cho be speed racer spe-0070', 'Flycam mini có camera cho bé | ToyLand', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 38880 MINUTE), DATE_SUB(NOW(), INTERVAL 38880 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'flycam-mini-co-camera-cho-be');
SET @p = (SELECT id FROM products WHERE slug = 'flycam-mini-co-camera-cho-be');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'SPE-0070' AS sku, '8930000000112' AS barcode, NULL AS option1, NULL AS option2, 1290000 AS price, NULL AS compare_at_price, 774000 AS cost_price, 4 AS stock_quantity, 450 AS weight_gram, 'https://placehold.co/600x600/455A64/3E2723/png?text=Flycam+mini+c+camera' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/455A64/3E2723/png?text=Flycam+mini+c+camera+ch' AS url, 'Flycam mini có camera cho bé' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Flycam+mini+c+camera+ch+2' AS url, 'Flycam mini có camera cho bé' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Flycam+mini+c+camera+ch+3' AS url, 'Flycam mini có camera cho bé' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Flycam+mini+c+camera+ch+4' AS url, 'Flycam mini có camera cho bé' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Camera' AS spec_key, '720p' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Thời gian bay' AS spec_key, '10 phút / pin' AS spec_value, 1 AS sort_order
  UNION ALL SELECT 'Chế độ' AS spec_key, 'Giữ độ cao, 1 nút cất/hạ cánh' AS spec_value, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 38880 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [32] Xe điều khiển leo tường
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-dieu-khien');
SET @brand = (SELECT id FROM brands WHERE slug = 'speed-racer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Xe điều khiển leo tường', 'xe-dieu-khien-leo-tuong', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', 'Xe điều khiển leo tường chính hãng Speed Racer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'UNISEX', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, 'Pin sạc (kèm theo)', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 490000, 490000, 15, 23, 0, 0, 'xe dieu khien leo tuong speed racer spe-0071', 'Xe điều khiển leo tường | ToyLand', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 90720 MINUTE), DATE_SUB(NOW(), INTERVAL 90720 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'xe-dieu-khien-leo-tuong');
SET @p = (SELECT id FROM products WHERE slug = 'xe-dieu-khien-leo-tuong');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'SPE-0071' AS sku, '8930000000113' AS barcode, NULL AS option1, NULL AS option2, 490000 AS price, NULL AS compare_at_price, 294000 AS cost_price, 15 AS stock_quantity, 500 AS weight_gram, 'https://placehold.co/600x600/7E57C2/3E2723/png?text=Xe+iu+khin+leo+tn' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/7E57C2/3E2723/png?text=Xe+iu+khin+leo+tng' AS url, 'Xe điều khiển leo tường' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Xe+iu+khin+leo+tng+2' AS url, 'Xe điều khiển leo tường' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Xe+iu+khin+leo+tng+3' AS url, 'Xe điều khiển leo tường' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Xe+iu+khin+leo+tng+4' AS url, 'Xe điều khiển leo tường' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Chế độ' AS spec_key, 'Chạy sàn / leo tường' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Thời gian chơi' AS spec_key, '15 phút' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 90720 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [33] Xe đua điều khiển drift 1:24  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'xe-dieu-khien');
SET @brand = (SELECT id FROM brands WHERE slug = 'speed-racer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Xe đua điều khiển drift 1:24', 'xe-dua-dieu-khien-drift-1-24', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', 'Xe drift tỉ lệ 1:24 dẫn động 4 bánh, lốp trơn chuyên dụng để ôm cua. Chọn màu và gói pin (1 pin hoặc 2 pin để chơi lâu gấp đôi).', @cat, @brand, 96, NULL, 'BOY', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, 'Pin sạc 7.4V', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Màu sắc', 'Bộ pin', 'ACTIVE', FALSE, FALSE, 690000, 790000, 19, 155, 0, 0, 'xe dua dieu khien drift 1:24 speed racer spe-0072-01 spe-0072-02 spe-0072-03 spe-0072-04', 'Xe đua điều khiển drift 1:24 | ToyLand', 'Đồ chơi Speed Racer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 30240 MINUTE), DATE_SUB(NOW(), INTERVAL 30240 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'xe-dua-dieu-khien-drift-1-24');
SET @p = (SELECT id FROM products WHERE slug = 'xe-dua-dieu-khien-drift-1-24');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'SPE-0072-01' AS sku, '8930000000114' AS barcode, 'Xanh lá' AS option1, '1 pin' AS option2, 690000 AS price, NULL AS compare_at_price, 414000 AS cost_price, 8 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/43A047/3E2723/png?text=Xanh+l+1+pin' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'SPE-0072-02' AS sku, '8930000000115' AS barcode, 'Xanh lá' AS option1, '2 pin' AS option2, 790000 AS price, NULL AS compare_at_price, 474000 AS cost_price, 5 AS stock_quantity, 760 AS weight_gram, 'https://placehold.co/600x600/43A047/3E2723/png?text=Xanh+l+2+pin' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'SPE-0072-03' AS sku, '8930000000116' AS barcode, 'Cam' AS option1, '1 pin' AS option2, 690000 AS price, NULL AS compare_at_price, 414000 AS cost_price, 6 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/43A047/3E2723/png?text=Cam+1+pin' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'SPE-0072-04' AS sku, '8930000000117' AS barcode, 'Cam' AS option1, '2 pin' AS option2, 790000 AS price, NULL AS compare_at_price, 474000 AS cost_price, 0 AS stock_quantity, 760 AS weight_gram, 'https://placehold.co/600x600/43A047/3E2723/png?text=Cam+2+pin' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/43A047/3E2723/png?text=Xe+ua+iu+khin+drift' AS url, 'Xe đua điều khiển drift 1:24' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Xe+ua+iu+khin+drift++2' AS url, 'Xe đua điều khiển drift 1:24' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Xe+ua+iu+khin+drift++3' AS url, 'Xe đua điều khiển drift 1:24' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Xe+ua+iu+khin+drift++4' AS url, 'Xe đua điều khiển drift 1:24' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Dẫn động' AS spec_key, '4 bánh (4WD)' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Lốp' AS spec_key, 'Lốp drift chuyên dụng' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 30240 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [34] Robokid Cánh tay robot thủy lực
SET @cat = (SELECT id FROM categories WHERE slug = 'stem-khoa-hoc');
SET @brand = (SELECT id FROM brands WHERE slug = 'robokid');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Robokid Cánh tay robot thủy lực', 'robokid-canh-tay-robot-thuy-luc', 'Đồ chơi Robokid chính hãng, an toàn cho bé', 'Robokid Cánh tay robot thủy lực chính hãng Robokid. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'UNISEX', 'Gỗ & ống xi lanh', 'Việt Nam', 'Việt Nam', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 390000, 390000, 22, 157, 0, 0, 'robokid canh tay robot thuy luc robokid rob-0073', 'Robokid Cánh tay robot thủy lực | ToyLand', 'Đồ chơi Robokid chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 67680 MINUTE), DATE_SUB(NOW(), INTERVAL 67680 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'robokid-canh-tay-robot-thuy-luc');
SET @p = (SELECT id FROM products WHERE slug = 'robokid-canh-tay-robot-thuy-luc');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'ROB-0073' AS sku, '8930000000118' AS barcode, NULL AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 22 AS stock_quantity, 800 AS weight_gram, 'https://placehold.co/600x600/26A69A/3E2723/png?text=Robokid+Cnh+tay+robot' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/26A69A/3E2723/png?text=Robokid+Cnh+tay+robot+t' AS url, 'Robokid Cánh tay robot thủy lực' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Robokid+Cnh+tay+robot+t+2' AS url, 'Robokid Cánh tay robot thủy lực' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Robokid+Cnh+tay+robot+t+3' AS url, 'Robokid Cánh tay robot thủy lực' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Robokid+Cnh+tay+robot+t+4' AS url, 'Robokid Cánh tay robot thủy lực' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Nguyên lý' AS spec_key, 'Thủy lực bằng nước' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Số khớp' AS spec_key, '4' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 67680 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [35] Bộ mạch điện tử lắp ghép Robokid  (2 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'stem-khoa-hoc');
SET @brand = (SELECT id FROM brands WHERE slug = 'robokid');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ mạch điện tử lắp ghép Robokid', 'bo-mach-dien-tu-lap-ghep-robokid', 'Đồ chơi Robokid chính hãng, an toàn cho bé', 'Bộ mạch điện tử lắp ghép Robokid chính hãng Robokid. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'UNISEX', 'Nhựa ABS', 'Việt Nam', 'Việt Nam', 2025, '2 pin AA (không kèm)', TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Phiên bản', NULL, 'ACTIVE', TRUE, FALSE, 890000, 1490000, 17, 109, 0, 0, 'bo mach dien tu lap ghep robokid robokid rob-0074-01 rob-0074-02', 'Bộ mạch điện tử lắp ghép Robokid | ToyLand', 'Đồ chơi Robokid chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 51840 MINUTE), DATE_SUB(NOW(), INTERVAL 51840 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-mach-dien-tu-lap-ghep-robokid');
SET @p = (SELECT id FROM products WHERE slug = 'bo-mach-dien-tu-lap-ghep-robokid');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'ROB-0074-01' AS sku, '8930000000119' AS barcode, '100 thí nghiệm' AS option1, NULL AS option2, 890000 AS price, NULL AS compare_at_price, 534000 AS cost_price, 12 AS stock_quantity, 900 AS weight_gram, 'https://placehold.co/600x600/00897B/3E2723/png?text=100+th+nghim' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'ROB-0074-02' AS sku, '8930000000120' AS barcode, '300 thí nghiệm' AS option1, NULL AS option2, 1490000 AS price, 1690000 AS compare_at_price, 894000 AS cost_price, 5 AS stock_quantity, 1400 AS weight_gram, 'https://placehold.co/600x600/00897B/3E2723/png?text=300+th+nghim' AS image_url, FALSE AS is_default, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/00897B/3E2723/png?text=B+mch+in+t+lp+ghp' AS url, 'Bộ mạch điện tử lắp ghép Robokid' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+mch+in+t+lp+ghp+2' AS url, 'Bộ mạch điện tử lắp ghép Robokid' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+mch+in+t+lp+ghp+3' AS url, 'Bộ mạch điện tử lắp ghép Robokid' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+mch+in+t+lp+ghp+4' AS url, 'Bộ mạch điện tử lắp ghép Robokid' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Kết nối' AS spec_key, 'Chốt bấm, không cần hàn' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kèm sách' AS spec_key, 'Hướng dẫn tiếng Việt' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 51840 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [36] Kính thiên văn khúc xạ cho bé 70mm
SET @cat = (SELECT id FROM categories WHERE slug = 'stem-khoa-hoc');
SET @brand = (SELECT id FROM brands WHERE slug = 'mideer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Kính thiên văn khúc xạ cho bé 70mm', 'kinh-thien-van-khuc-xa-cho-be-70mm', 'Đồ chơi Mideer chính hãng, an toàn cho bé', 'Kính thiên văn khúc xạ cho bé 70mm chính hãng Mideer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'UNISEX', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 80, 20, 20, NULL, NULL, 'ACTIVE', FALSE, FALSE, 1190000, 1190000, 6, 20, 0, 0, 'kinh thien van khuc xa cho be 70mm mideer mid-0075', 'Kính thiên văn khúc xạ cho bé 70mm | ToyLand', 'Đồ chơi Mideer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 103680 MINUTE), DATE_SUB(NOW(), INTERVAL 103680 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'kinh-thien-van-khuc-xa-cho-be-70mm');
SET @p = (SELECT id FROM products WHERE slug = 'kinh-thien-van-khuc-xa-cho-be-70mm');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MID-0075' AS sku, '8930000000121' AS barcode, NULL AS option1, NULL AS option2, 1190000 AS price, NULL AS compare_at_price, 714000 AS cost_price, 6 AS stock_quantity, 2500 AS weight_gram, 'https://placehold.co/600x600/283593/3E2723/png?text=Knh+thin+vn+khc+x' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/283593/3E2723/png?text=Knh+thin+vn+khc+x+c' AS url, 'Kính thiên văn khúc xạ cho bé 70mm' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Knh+thin+vn+khc+x+c+2' AS url, 'Kính thiên văn khúc xạ cho bé 70mm' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Knh+thin+vn+khc+x+c+3' AS url, 'Kính thiên văn khúc xạ cho bé 70mm' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Knh+thin+vn+khc+x+c+4' AS url, 'Kính thiên văn khúc xạ cho bé 70mm' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Đường kính vật kính' AS spec_key, '70 mm' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Độ phóng đại' AS spec_key, '20x - 150x' AS spec_value, 1 AS sort_order
  UNION ALL SELECT 'Chân đế' AS spec_key, 'Nhôm, gấp gọn' AS spec_value, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 103680 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [37] Bộ trồng cây thủy canh mini
SET @cat = (SELECT id FROM categories WHERE slug = 'stem-khoa-hoc');
SET @brand = (SELECT id FROM brands WHERE slug = 'mideer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ trồng cây thủy canh mini', 'bo-trong-cay-thuy-canh-mini', 'Đồ chơi Mideer chính hãng, an toàn cho bé', 'Bộ trồng cây thủy canh mini chính hãng Mideer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'UNISEX', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 250000, 250000, 30, 64, 0, 0, 'bo trong cay thuy canh mini mideer mid-0076', 'Bộ trồng cây thủy canh mini | ToyLand', 'Đồ chơi Mideer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 8640 MINUTE), DATE_SUB(NOW(), INTERVAL 8640 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-trong-cay-thuy-canh-mini');
SET @p = (SELECT id FROM products WHERE slug = 'bo-trong-cay-thuy-canh-mini');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MID-0076' AS sku, '8930000000122' AS barcode, NULL AS option1, NULL AS option2, 250000 AS price, NULL AS compare_at_price, 150000 AS cost_price, 30 AS stock_quantity, 600 AS weight_gram, 'https://placehold.co/600x600/7CB342/3E2723/png?text=B+trng+cy+thy+canh' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/7CB342/3E2723/png?text=B+trng+cy+thy+canh+m' AS url, 'Bộ trồng cây thủy canh mini' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+trng+cy+thy+canh+m+2' AS url, 'Bộ trồng cây thủy canh mini' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+trng+cy+thy+canh+m+3' AS url, 'Bộ trồng cây thủy canh mini' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+trng+cy+thy+canh+m+4' AS url, 'Bộ trồng cây thủy canh mini' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Kèm hạt giống' AS spec_key, '3 loại rau mầm' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Thời gian nảy mầm' AS spec_key, '3 - 7 ngày' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 8640 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [38] Rubik's Cube chính hãng  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'stem-khoa-hoc');
SET @brand = (SELECT id FROM brands WHERE slug = 'rubik-s');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Rubik''s Cube chính hãng', 'rubik-s-cube-chinh-hang', 'Đồ chơi Rubik''s chính hãng, an toàn cho bé', 'Rubik''s Cube chính hãng chính hãng Rubik''s. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'UNISEX', 'Nhựa ABS', 'Hungary', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Loại', NULL, 'ACTIVE', FALSE, FALSE, 199000, 399000, 78, 19, 0, 0, 'rubik''s cube chinh hang rubik''s rub-0077-01 rub-0077-02 rub-0077-03 rub-0077-04', 'Rubik''s Cube chính hãng | ToyLand', 'Đồ chơi Rubik''s chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 144000 MINUTE), DATE_SUB(NOW(), INTERVAL 144000 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'rubik-s-cube-chinh-hang');
SET @p = (SELECT id FROM products WHERE slug = 'rubik-s-cube-chinh-hang');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'RUB-0077-01' AS sku, '8930000000123' AS barcode, '2x2' AS option1, NULL AS option2, 199000 AS price, NULL AS compare_at_price, 119000 AS cost_price, 20 AS stock_quantity, 80 AS weight_gram, 'https://placehold.co/600x600/FDD835/3E2723/png?text=2x2' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'RUB-0077-02' AS sku, '8930000000124' AS barcode, '3x3' AS option1, NULL AS option2, 249000 AS price, NULL AS compare_at_price, 149000 AS cost_price, 45 AS stock_quantity, 100 AS weight_gram, 'https://placehold.co/600x600/FDD835/3E2723/png?text=3x3' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'RUB-0077-03' AS sku, '8930000000125' AS barcode, '3x3 Speed' AS option1, NULL AS option2, 349000 AS price, NULL AS compare_at_price, 209000 AS cost_price, 10 AS stock_quantity, 90 AS weight_gram, 'https://placehold.co/600x600/FDD835/3E2723/png?text=3x3+Speed' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'RUB-0077-04' AS sku, '8930000000126' AS barcode, '4x4' AS option1, NULL AS option2, 399000 AS price, NULL AS compare_at_price, 239000 AS cost_price, 3 AS stock_quantity, 150 AS weight_gram, 'https://placehold.co/600x600/FDD835/3E2723/png?text=4x4' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FDD835/3E2723/png?text=Rubiks+Cube+chnh+hng' AS url, 'Rubik''s Cube chính hãng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Rubiks+Cube+chnh+hng+2' AS url, 'Rubik''s Cube chính hãng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Rubiks+Cube+chnh+hng+3' AS url, 'Rubik''s Cube chính hãng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Rubiks+Cube+chnh+hng+4' AS url, 'Rubik''s Cube chính hãng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Kỹ năng' AS spec_key, 'Tư duy không gian, trí nhớ' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 144000 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [39] Robot lập trình Robokid Coding Bot
SET @cat = (SELECT id FROM categories WHERE slug = 'stem-khoa-hoc');
SET @brand = (SELECT id FROM brands WHERE slug = 'robokid');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Robot lập trình Robokid Coding Bot', 'robot-lap-trinh-robokid-coding-bot', 'Đồ chơi Robokid chính hãng, an toàn cho bé', 'Robot lập trình Robokid Coding Bot chính hãng Robokid. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'UNISEX', 'Nhựa ABS', 'Việt Nam', 'Việt Nam', 2025, 'Pin sạc (kèm cáp USB-C)', TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', TRUE, TRUE, 1590000, 1590000, 9, 150, 0, 0, 'robot lap trinh robokid coding bot robokid rob-0078', 'Robot lập trình Robokid Coding Bot | ToyLand', 'Đồ chơi Robokid chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 11520 MINUTE), DATE_SUB(NOW(), INTERVAL 11520 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'robot-lap-trinh-robokid-coding-bot');
SET @p = (SELECT id FROM products WHERE slug = 'robot-lap-trinh-robokid-coding-bot');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'ROB-0078' AS sku, '8930000000127' AS barcode, NULL AS option1, NULL AS option2, 1590000 AS price, 1890000 AS compare_at_price, 954000 AS cost_price, 9 AS stock_quantity, 900 AS weight_gram, 'https://placehold.co/600x600/5C6BC0/3E2723/png?text=Robot+lp+trnh+Roboki' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/5C6BC0/3E2723/png?text=Robot+lp+trnh+Robokid' AS url, 'Robot lập trình Robokid Coding Bot' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Robot+lp+trnh+Robokid++2' AS url, 'Robot lập trình Robokid Coding Bot' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Robot+lp+trnh+Robokid++3' AS url, 'Robot lập trình Robokid Coding Bot' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Robot+lp+trnh+Robokid++4' AS url, 'Robot lập trình Robokid Coding Bot' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Lập trình' AS spec_key, 'Thẻ lệnh & app kéo thả' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Cảm biến' AS spec_key, 'Dò line, tránh vật cản' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 11520 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [40] Fisher-Price Cún con học nói song ngữ
SET @cat = (SELECT id FROM categories WHERE slug = 'hoc-chu-hoc-so');
SET @brand = (SELECT id FROM brands WHERE slug = 'fisher-price');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Fisher-Price Cún con học nói song ngữ', 'fisher-price-cun-con-hoc-noi-song-ngu', 'Đồ chơi Fisher-Price chính hãng, an toàn cho bé', 'Fisher-Price Cún con học nói song ngữ chính hãng Fisher-Price. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 6, 36, 'UNISEX', 'Vải nhung & nhựa', 'Mỹ', 'Trung Quốc', 2025, '3 pin AA (kèm theo)', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', TRUE, FALSE, 690000, 690000, 17, 42, 0, 0, 'fisher-price cun con hoc noi song ngu fisher-price fis-0079', 'Fisher-Price Cún con học nói song ngữ | ToyLand', 'Đồ chơi Fisher-Price chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 41760 MINUTE), DATE_SUB(NOW(), INTERVAL 41760 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'fisher-price-cun-con-hoc-noi-song-ngu');
SET @p = (SELECT id FROM products WHERE slug = 'fisher-price-cun-con-hoc-noi-song-ngu');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'FIS-0079' AS sku, '8930000000128' AS barcode, NULL AS option1, NULL AS option2, 690000 AS price, NULL AS compare_at_price, 414000 AS cost_price, 17 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/FFB74D/3E2723/png?text=FisherPrice+Cn+con+h' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FFB74D/3E2723/png?text=FisherPrice+Cn+con+hc' AS url, 'Fisher-Price Cún con học nói song ngữ' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=FisherPrice+Cn+con+hc+2' AS url, 'Fisher-Price Cún con học nói song ngữ' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=FisherPrice+Cn+con+hc+3' AS url, 'Fisher-Price Cún con học nói song ngữ' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=FisherPrice+Cn+con+hc+4' AS url, 'Fisher-Price Cún con học nói song ngữ' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Ngôn ngữ' AS spec_key, 'Tiếng Việt & Tiếng Anh' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Số bài hát' AS spec_key, 'Hơn 40' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 41760 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [41] VTech Laptop học tập song ngữ
SET @cat = (SELECT id FROM categories WHERE slug = 'hoc-chu-hoc-so');
SET @brand = (SELECT id FROM brands WHERE slug = 'vtech');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'VTech Laptop học tập song ngữ', 'vtech-laptop-hoc-tap-song-ngu', 'Đồ chơi VTech chính hãng, an toàn cho bé', 'VTech Laptop học tập song ngữ chính hãng VTech. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 48, 96, 'UNISEX', 'Nhựa ABS', 'Hồng Kông', 'Trung Quốc', 2025, '3 pin AA (kèm theo)', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 990000, 990000, 10, 82, 0, 0, 'vtech laptop hoc tap song ngu vtech vte-0080', 'VTech Laptop học tập song ngữ | ToyLand', 'Đồ chơi VTech chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 126720 MINUTE), DATE_SUB(NOW(), INTERVAL 126720 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'vtech-laptop-hoc-tap-song-ngu');
SET @p = (SELECT id FROM products WHERE slug = 'vtech-laptop-hoc-tap-song-ngu');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'VTE-0080' AS sku, '8930000000129' AS barcode, NULL AS option1, NULL AS option2, 990000 AS price, NULL AS compare_at_price, 594000 AS cost_price, 10 AS stock_quantity, 1000 AS weight_gram, 'https://placehold.co/600x600/039BE5/3E2723/png?text=VTech+Laptop+hc+tp+s' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/039BE5/3E2723/png?text=VTech+Laptop+hc+tp+son' AS url, 'VTech Laptop học tập song ngữ' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=VTech+Laptop+hc+tp+son+2' AS url, 'VTech Laptop học tập song ngữ' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=VTech+Laptop+hc+tp+son+3' AS url, 'VTech Laptop học tập song ngữ' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=VTech+Laptop+hc+tp+son+4' AS url, 'VTech Laptop học tập song ngữ' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số hoạt động' AS spec_key, '60' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Màn hình' AS spec_key, 'LCD' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 126720 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [42] Bộ ghép hình gỗ Mideer  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'hoc-chu-hoc-so');
SET @brand = (SELECT id FROM brands WHERE slug = 'mideer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ ghép hình gỗ Mideer', 'bo-ghep-hinh-go-mideer', 'Đồ chơi Mideer chính hãng, an toàn cho bé', 'Bộ ghép hình gỗ Mideer chính hãng Mideer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 24, 72, 'UNISEX', 'Gỗ', 'Trung Quốc', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Chủ đề', NULL, 'ACTIVE', FALSE, FALSE, 159000, 169000, 54, 115, 0, 0, 'bo ghep hinh go mideer mideer mid-0081-01 mid-0081-02 mid-0081-03', 'Bộ ghép hình gỗ Mideer | ToyLand', 'Đồ chơi Mideer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 59040 MINUTE), DATE_SUB(NOW(), INTERVAL 59040 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-ghep-hinh-go-mideer');
SET @p = (SELECT id FROM products WHERE slug = 'bo-ghep-hinh-go-mideer');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MID-0081-01' AS sku, '8930000000130' AS barcode, 'Chữ cái' AS option1, NULL AS option2, 159000 AS price, NULL AS compare_at_price, 95000 AS cost_price, 30 AS stock_quantity, 500 AS weight_gram, 'https://placehold.co/600x600/AED581/3E2723/png?text=Ch+ci' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'MID-0081-02' AS sku, '8930000000131' AS barcode, 'Chữ số' AS option1, NULL AS option2, 159000 AS price, NULL AS compare_at_price, 95000 AS cost_price, 24 AS stock_quantity, 500 AS weight_gram, 'https://placehold.co/600x600/AED581/3E2723/png?text=Ch+s' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'MID-0081-03' AS sku, '8930000000132' AS barcode, 'Con vật' AS option1, NULL AS option2, 169000 AS price, NULL AS compare_at_price, 101000 AS cost_price, 0 AS stock_quantity, 500 AS weight_gram, 'https://placehold.co/600x600/AED581/3E2723/png?text=Con+vt' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/AED581/3E2723/png?text=B+ghp+hnh+g+Mideer' AS url, 'Bộ ghép hình gỗ Mideer' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+ghp+hnh+g+Mideer+2' AS url, 'Bộ ghép hình gỗ Mideer' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+ghp+hnh+g+Mideer+3' AS url, 'Bộ ghép hình gỗ Mideer' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+ghp+hnh+g+Mideer+4' AS url, 'Bộ ghép hình gỗ Mideer' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Sơn' AS spec_key, 'Gốc nước, an toàn cho bé' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 59040 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [43] Bộ bột nặn Play-Doh  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'nghe-thuat-sang-tao');
SET @brand = (SELECT id FROM brands WHERE slug = 'play-doh');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ bột nặn Play-Doh', 'bo-bot-nan-play-doh', 'Đồ chơi Play-Doh chính hãng, an toàn cho bé', 'Bộ bột nặn Play-Doh chính hãng Play-Doh. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 24, NULL, 'UNISEX', 'Bột nặn gốc bột mì', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Không ăn. Chứa bột mì – không phù hợp trẻ dị ứng gluten.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Bộ', NULL, 'ACTIVE', TRUE, FALSE, 99000, 449000, 107, 44, 0, 0, 'bo bot nan play-doh play-doh pla-0082-01 pla-0082-02 pla-0082-03', 'Bộ bột nặn Play-Doh | ToyLand', 'Đồ chơi Play-Doh chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 23040 MINUTE), DATE_SUB(NOW(), INTERVAL 23040 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-bot-nan-play-doh');
SET @p = (SELECT id FROM products WHERE slug = 'bo-bot-nan-play-doh');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'PLA-0082-01' AS sku, '8930000000133' AS barcode, '4 hộp' AS option1, NULL AS option2, 99000 AS price, NULL AS compare_at_price, 59000 AS cost_price, 60 AS stock_quantity, 250 AS weight_gram, 'https://placehold.co/600x600/EF5350/3E2723/png?text=4+hp' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'PLA-0082-02' AS sku, '8930000000134' AS barcode, '10 hộp' AS option1, NULL AS option2, 229000 AS price, 259000 AS compare_at_price, 137000 AS cost_price, 35 AS stock_quantity, 600 AS weight_gram, 'https://placehold.co/600x600/EF5350/3E2723/png?text=10+hp' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'PLA-0082-03' AS sku, '8930000000135' AS barcode, '24 hộp' AS option1, NULL AS option2, 449000 AS price, 499000 AS compare_at_price, 269000 AS cost_price, 12 AS stock_quantity, 1400 AS weight_gram, 'https://placehold.co/600x600/EF5350/3E2723/png?text=24+hp' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/EF5350/3E2723/png?text=B+bt+nn+PlayDoh' AS url, 'Bộ bột nặn Play-Doh' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+bt+nn+PlayDoh+2' AS url, 'Bộ bột nặn Play-Doh' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+bt+nn+PlayDoh+3' AS url, 'Bộ bột nặn Play-Doh' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+bt+nn+PlayDoh+4' AS url, 'Bộ bột nặn Play-Doh' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 23040 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [44] Play-Doh Tiệm kem ngọt ngào
SET @cat = (SELECT id FROM categories WHERE slug = 'nghe-thuat-sang-tao');
SET @brand = (SELECT id FROM brands WHERE slug = 'play-doh');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Play-Doh Tiệm kem ngọt ngào', 'play-doh-tiem-kem-ngot-ngao', 'Đồ chơi Play-Doh chính hãng, an toàn cho bé', 'Play-Doh Tiệm kem ngọt ngào chính hãng Play-Doh. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 390000, 390000, 14, 146, 0, 0, 'play-doh tiem kem ngot ngao play-doh pla-0083', 'Play-Doh Tiệm kem ngọt ngào | ToyLand', 'Đồ chơi Play-Doh chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 77760 MINUTE), DATE_SUB(NOW(), INTERVAL 77760 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'play-doh-tiem-kem-ngot-ngao');
SET @p = (SELECT id FROM products WHERE slug = 'play-doh-tiem-kem-ngot-ngao');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'PLA-0083' AS sku, '8930000000136' AS barcode, NULL AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 14 AS stock_quantity, 800 AS weight_gram, 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=PlayDoh+Tim+kem+ngt' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=PlayDoh+Tim+kem+ngt+n' AS url, 'Play-Doh Tiệm kem ngọt ngào' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=PlayDoh+Tim+kem+ngt+n+2' AS url, 'Play-Doh Tiệm kem ngọt ngào' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=PlayDoh+Tim+kem+ngt+n+3' AS url, 'Play-Doh Tiệm kem ngọt ngào' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=PlayDoh+Tim+kem+ngt+n+4' AS url, 'Play-Doh Tiệm kem ngọt ngào' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số hộp bột' AS spec_key, '5' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Dụng cụ' AS spec_key, 'Máy ép kem, khuôn, thìa' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 77760 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [45] Bảng vẽ điện tử LCD 10 inch  (2 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'nghe-thuat-sang-tao');
SET @brand = (SELECT id FROM brands WHERE slug = 'mideer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bảng vẽ điện tử LCD 10 inch', 'bang-ve-dien-tu-lcd-10-inch', 'Đồ chơi Mideer chính hãng, an toàn cho bé', 'Bảng vẽ điện tử LCD 10 inch chính hãng Mideer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, 'Pin cúc áo CR2025 (kèm theo)', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Màu sắc', NULL, 'ACTIVE', FALSE, FALSE, 159000, 159000, 47, 38, 0, 0, 'bang ve dien tu lcd 10 inch mideer mid-0084-01 mid-0084-02', 'Bảng vẽ điện tử LCD 10 inch | ToyLand', 'Đồ chơi Mideer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 31680 MINUTE), DATE_SUB(NOW(), INTERVAL 31680 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bang-ve-dien-tu-lcd-10-inch');
SET @p = (SELECT id FROM products WHERE slug = 'bang-ve-dien-tu-lcd-10-inch');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MID-0084-01' AS sku, '8930000000137' AS barcode, 'Hồng' AS option1, NULL AS option2, 159000 AS price, 199000 AS compare_at_price, 95000 AS cost_price, 28 AS stock_quantity, 250 AS weight_gram, 'https://placehold.co/600x600/4DD0E1/3E2723/png?text=Hng' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'MID-0084-02' AS sku, '8930000000138' AS barcode, 'Xanh dương' AS option1, NULL AS option2, 159000 AS price, 199000 AS compare_at_price, 95000 AS cost_price, 19 AS stock_quantity, 250 AS weight_gram, 'https://placehold.co/600x600/4DD0E1/3E2723/png?text=Xanh+dng' AS image_url, FALSE AS is_default, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/4DD0E1/3E2723/png?text=Bng+v+in+t+LCD+10+i' AS url, 'Bảng vẽ điện tử LCD 10 inch' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Bng+v+in+t+LCD+10+i+2' AS url, 'Bảng vẽ điện tử LCD 10 inch' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Bng+v+in+t+LCD+10+i+3' AS url, 'Bảng vẽ điện tử LCD 10 inch' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Bng+v+in+t+LCD+10+i+4' AS url, 'Bảng vẽ điện tử LCD 10 inch' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Kích thước màn hình' AS spec_key, '10 inch' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Xoá' AS spec_key, '1 nút bấm' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 31680 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [46] Bộ bút sáp màu & tập tô 48 món
SET @cat = (SELECT id FROM categories WHERE slug = 'nghe-thuat-sang-tao');
SET @brand = (SELECT id FROM brands WHERE slug = 'mideer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ bút sáp màu & tập tô 48 món', 'bo-but-sap-mau-tap-to-48-mon', 'Đồ chơi Mideer chính hãng, an toàn cho bé', 'Bộ bút sáp màu & tập tô 48 món chính hãng Mideer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 189000, 189000, 26, 154, 0, 0, 'bo but sap mau & tap to 48 mon mideer mid-0085', 'Bộ bút sáp màu & tập tô 48 món | ToyLand', 'Đồ chơi Mideer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 92160 MINUTE), DATE_SUB(NOW(), INTERVAL 92160 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-but-sap-mau-tap-to-48-mon');
SET @p = (SELECT id FROM products WHERE slug = 'bo-but-sap-mau-tap-to-48-mon');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MID-0085' AS sku, '8930000000139' AS barcode, NULL AS option1, NULL AS option2, 189000 AS price, NULL AS compare_at_price, 113000 AS cost_price, 26 AS stock_quantity, 500 AS weight_gram, 'https://placehold.co/600x600/FFB300/3E2723/png?text=B+bt+sp+mu++tp+t' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FFB300/3E2723/png?text=B+bt+sp+mu++tp+t' AS url, 'Bộ bút sáp màu & tập tô 48 món' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+bt+sp+mu++tp+t++2' AS url, 'Bộ bút sáp màu & tập tô 48 món' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+bt+sp+mu++tp+t++3' AS url, 'Bộ bút sáp màu & tập tô 48 món' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+bt+sp+mu++tp+t++4' AS url, 'Bộ bút sáp màu & tập tô 48 món' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số màu' AS spec_key, '24' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kèm theo' AS spec_key, 'Tập tô 20 trang, gọt bút' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 92160 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [47] Gấu bông Doraemon  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'thu-bong');
SET @brand = (SELECT id FROM brands WHERE slug = 'teddy-viet');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Gấu bông Doraemon', 'gau-bong-doraemon', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', 'Gấu bông Doraemon chính hãng Teddy Việt. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 0, NULL, 'UNISEX', 'Vải nhung, bông PP', 'Việt Nam', 'Việt Nam', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Kích thước', NULL, 'ACTIVE', TRUE, FALSE, 159000, 499000, 61, 86, 0, 0, 'gau bong doraemon teddy viet doraemon ted-0086-01 ted-0086-02 ted-0086-03', 'Gấu bông Doraemon | ToyLand', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 27360 MINUTE), DATE_SUB(NOW(), INTERVAL 27360 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'gau-bong-doraemon');
SET @p = (SELECT id FROM products WHERE slug = 'gau-bong-doraemon');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'TED-0086-01' AS sku, '8930000000140' AS barcode, '25 cm' AS option1, NULL AS option2, 159000 AS price, NULL AS compare_at_price, 95000 AS cost_price, 40 AS stock_quantity, 200 AS weight_gram, 'https://placehold.co/600x600/29B6F6/3E2723/png?text=25+cm' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'TED-0086-02' AS sku, '8930000000141' AS barcode, '45 cm' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 18 AS stock_quantity, 450 AS weight_gram, 'https://placehold.co/600x600/29B6F6/3E2723/png?text=45+cm' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'TED-0086-03' AS sku, '8930000000142' AS barcode, '70 cm' AS option1, NULL AS option2, 499000 AS price, 590000 AS compare_at_price, 299000 AS cost_price, 3 AS stock_quantity, 900 AS weight_gram, 'https://placehold.co/600x600/29B6F6/3E2723/png?text=70+cm' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/29B6F6/3E2723/png?text=Gu+bng+Doraemon' AS url, 'Gấu bông Doraemon' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Gu+bng+Doraemon+2' AS url, 'Gấu bông Doraemon' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Gu+bng+Doraemon+3' AS url, 'Gấu bông Doraemon' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Gu+bng+Doraemon+4' AS url, 'Gấu bông Doraemon' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'doraemon';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 27360 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [48] Thỏ bông tai dài  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'thu-bong');
SET @brand = (SELECT id FROM brands WHERE slug = 'teddy-viet');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Thỏ bông tai dài', 'tho-bong-tai-dai', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', 'Thỏ bông tai dài chính hãng Teddy Việt. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 0, NULL, 'UNISEX', 'Vải nhung, bông PP', 'Việt Nam', 'Việt Nam', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Kích thước', 'Màu sắc', 'ACTIVE', FALSE, FALSE, 179000, 329000, 54, 151, 0, 0, 'tho bong tai dai teddy viet ted-0087-01 ted-0087-02 ted-0087-03 ted-0087-04', 'Thỏ bông tai dài | ToyLand', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 44640 MINUTE), DATE_SUB(NOW(), INTERVAL 44640 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'tho-bong-tai-dai');
SET @p = (SELECT id FROM products WHERE slug = 'tho-bong-tai-dai');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'TED-0087-01' AS sku, '8930000000143' AS barcode, '35 cm' AS option1, 'Trắng' AS option2, 179000 AS price, NULL AS compare_at_price, 107000 AS cost_price, 25 AS stock_quantity, 250 AS weight_gram, 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=35+cm+Trng' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'TED-0087-02' AS sku, '8930000000144' AS barcode, '35 cm' AS option1, 'Hồng' AS option2, 179000 AS price, NULL AS compare_at_price, 107000 AS cost_price, 20 AS stock_quantity, 250 AS weight_gram, 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=35+cm+Hng' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'TED-0087-03' AS sku, '8930000000145' AS barcode, '60 cm' AS option1, 'Trắng' AS option2, 329000 AS price, NULL AS compare_at_price, 197000 AS cost_price, 9 AS stock_quantity, 550 AS weight_gram, 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=60+cm+Trng' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'TED-0087-04' AS sku, '8930000000146' AS barcode, '60 cm' AS option1, 'Hồng' AS option2, 329000 AS price, NULL AS compare_at_price, 197000 AS cost_price, 0 AS stock_quantity, 550 AS weight_gram, 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=60+cm+Hng' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F8BBD0/3E2723/png?text=Th+bng+tai+di' AS url, 'Thỏ bông tai dài' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Th+bng+tai+di+2' AS url, 'Thỏ bông tai dài' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Th+bng+tai+di+3' AS url, 'Thỏ bông tai dài' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Th+bng+tai+di+4' AS url, 'Thỏ bông tai dài' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Giặt được' AS spec_key, 'Có, giặt tay' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 44640 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [49] Gối ôm cá mập  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'thu-bong');
SET @brand = (SELECT id FROM brands WHERE slug = 'teddy-viet');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Gối ôm cá mập', 'goi-om-ca-map', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', 'Gối ôm cá mập chính hãng Teddy Việt. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 12, NULL, 'UNISEX', 'Vải co giãn, bông PP', 'Việt Nam', 'Việt Nam', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Kích thước', NULL, 'ACTIVE', FALSE, FALSE, 199000, 549000, 49, 54, 0, 0, 'goi om ca map teddy viet ted-0088-01 ted-0088-02 ted-0088-03', 'Gối ôm cá mập | ToyLand', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 97920 MINUTE), DATE_SUB(NOW(), INTERVAL 97920 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'goi-om-ca-map');
SET @p = (SELECT id FROM products WHERE slug = 'goi-om-ca-map');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'TED-0088-01' AS sku, '8930000000147' AS barcode, '60 cm' AS option1, NULL AS option2, 199000 AS price, NULL AS compare_at_price, 119000 AS cost_price, 30 AS stock_quantity, 400 AS weight_gram, 'https://placehold.co/600x600/64B5F6/3E2723/png?text=60+cm' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'TED-0088-02' AS sku, '8930000000148' AS barcode, '100 cm' AS option1, NULL AS option2, 349000 AS price, NULL AS compare_at_price, 209000 AS cost_price, 15 AS stock_quantity, 800 AS weight_gram, 'https://placehold.co/600x600/64B5F6/3E2723/png?text=100+cm' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'TED-0088-03' AS sku, '8930000000149' AS barcode, '140 cm' AS option1, NULL AS option2, 549000 AS price, 650000 AS compare_at_price, 329000 AS cost_price, 4 AS stock_quantity, 1400 AS weight_gram, 'https://placehold.co/600x600/64B5F6/3E2723/png?text=140+cm' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/64B5F6/3E2723/png?text=Gi+m+c+mp' AS url, 'Gối ôm cá mập' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Gi+m+c+mp+2' AS url, 'Gối ôm cá mập' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Gi+m+c+mp+3' AS url, 'Gối ôm cá mập' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Gi+m+c+mp+4' AS url, 'Gối ôm cá mập' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 97920 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [50] Thú bông Pokémon Snorlax khổng lồ
SET @cat = (SELECT id FROM categories WHERE slug = 'thu-bong');
SET @brand = (SELECT id FROM brands WHERE slug = 'pokemon');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Thú bông Pokémon Snorlax khổng lồ', 'thu-bong-pokemon-snorlax-khong-lo', 'Đồ chơi Pokémon chính hãng, an toàn cho bé', 'Thú bông Pokémon Snorlax khổng lồ chính hãng Pokémon. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Vải nhung', 'Nhật Bản', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 60, 50, 40, NULL, NULL, 'ACTIVE', FALSE, FALSE, 1290000, 1290000, 2, 34, 0, 0, 'thu bong pokemon snorlax khong lo pokemon pokemon pok-0089', 'Thú bông Pokémon Snorlax khổng lồ | ToyLand', 'Đồ chơi Pokémon chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 18720 MINUTE), DATE_SUB(NOW(), INTERVAL 18720 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'thu-bong-pokemon-snorlax-khong-lo');
SET @p = (SELECT id FROM products WHERE slug = 'thu-bong-pokemon-snorlax-khong-lo');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'POK-0089' AS sku, '8930000000150' AS barcode, NULL AS option1, NULL AS option2, 1290000 AS price, NULL AS compare_at_price, 774000 AS cost_price, 2 AS stock_quantity, 2200 AS weight_gram, 'https://placehold.co/600x600/26A69A/3E2723/png?text=Th+bng+Pokmon+Snorl' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/26A69A/3E2723/png?text=Th+bng+Pokmon+Snorlax' AS url, 'Thú bông Pokémon Snorlax khổng lồ' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Th+bng+Pokmon+Snorlax+2' AS url, 'Thú bông Pokémon Snorlax khổng lồ' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Th+bng+Pokmon+Snorlax+3' AS url, 'Thú bông Pokémon Snorlax khổng lồ' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Th+bng+Pokmon+Snorlax+4' AS url, 'Thú bông Pokémon Snorlax khổng lồ' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Kích thước' AS spec_key, '60 cm' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'pokemon';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 18720 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [51] Kỳ lân bông phát sáng  (2 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'thu-bong');
SET @brand = (SELECT id FROM brands WHERE slug = 'teddy-viet');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Kỳ lân bông phát sáng', 'ky-lan-bong-phat-sang', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', 'Kỳ lân bông phát sáng chính hãng Teddy Việt. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'GIRL', 'Vải nhung', 'Việt Nam', 'Việt Nam', 2025, '3 pin LR44 (kèm theo)', FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Màu sắc', NULL, 'ACTIVE', FALSE, FALSE, 299000, 299000, 27, 156, 0, 0, 'ky lan bong phat sang teddy viet ted-0090-01 ted-0090-02', 'Kỳ lân bông phát sáng | ToyLand', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 61920 MINUTE), DATE_SUB(NOW(), INTERVAL 61920 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'ky-lan-bong-phat-sang');
SET @p = (SELECT id FROM products WHERE slug = 'ky-lan-bong-phat-sang');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'TED-0090-01' AS sku, '8930000000151' AS barcode, 'Hồng' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 16 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/CE93D8/3E2723/png?text=Hng' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'TED-0090-02' AS sku, '8930000000152' AS barcode, 'Tím' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 11 AS stock_quantity, 350 AS weight_gram, 'https://placehold.co/600x600/CE93D8/3E2723/png?text=Tm' AS image_url, FALSE AS is_default, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/CE93D8/3E2723/png?text=K+ln+bng+pht+sng' AS url, 'Kỳ lân bông phát sáng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=K+ln+bng+pht+sng+2' AS url, 'Kỳ lân bông phát sáng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=K+ln+bng+pht+sng+3' AS url, 'Kỳ lân bông phát sáng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=K+ln+bng+pht+sng+4' AS url, 'Kỳ lân bông phát sáng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Hiệu ứng' AS spec_key, 'Sừng phát sáng 7 màu' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 61920 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [52] Cờ vua nam châm gấp gọn  (2 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'board-game');
SET @brand = (SELECT id FROM brands WHERE slug = 'mideer');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Cờ vua nam châm gấp gọn', 'co-vua-nam-cham-gap-gon', 'Đồ chơi Mideer chính hãng, an toàn cho bé', 'Cờ vua nam châm gấp gọn chính hãng Mideer. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'UNISEX', 'Nhựa ABS', 'Trung Quốc', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Kích thước', NULL, 'ACTIVE', FALSE, FALSE, 199000, 299000, 32, 154, 0, 0, 'co vua nam cham gap gon mideer mid-0091-01 mid-0091-02', 'Cờ vua nam châm gấp gọn | ToyLand', 'Đồ chơi Mideer chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 82080 MINUTE), DATE_SUB(NOW(), INTERVAL 82080 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'co-vua-nam-cham-gap-gon');
SET @p = (SELECT id FROM products WHERE slug = 'co-vua-nam-cham-gap-gon');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MID-0091-01' AS sku, '8930000000153' AS barcode, '25 cm' AS option1, NULL AS option2, 199000 AS price, NULL AS compare_at_price, 119000 AS cost_price, 22 AS stock_quantity, 500 AS weight_gram, 'https://placehold.co/600x600/6D4C41/3E2723/png?text=25+cm' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'MID-0091-02' AS sku, '8930000000154' AS barcode, '32 cm' AS option1, NULL AS option2, 299000 AS price, NULL AS compare_at_price, 179000 AS cost_price, 10 AS stock_quantity, 800 AS weight_gram, 'https://placehold.co/600x600/6D4C41/3E2723/png?text=32+cm' AS image_url, FALSE AS is_default, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/6D4C41/3E2723/png?text=C+vua+nam+chm+gp+gn' AS url, 'Cờ vua nam châm gấp gọn' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=C+vua+nam+chm+gp+gn+2' AS url, 'Cờ vua nam châm gấp gọn' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=C+vua+nam+chm+gp+gn+3' AS url, 'Cờ vua nam châm gấp gọn' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=C+vua+nam+chm+gp+gn+4' AS url, 'Cờ vua nam châm gấp gọn' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số người chơi' AS spec_key, '2' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Quân cờ' AS spec_key, 'Nam châm, không rơi' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 82080 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [53] Bài Ma Sói Ultimate Việt hoá
SET @cat = (SELECT id FROM categories WHERE slug = 'board-game');
SET @brand = (SELECT id FROM brands WHERE slug = 'boardgame-vn');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bài Ma Sói Ultimate Việt hoá', 'bai-ma-soi-ultimate-viet-hoa', 'Đồ chơi BoardGame VN chính hãng, an toàn cho bé', 'Bài Ma Sói Ultimate Việt hoá chính hãng BoardGame VN. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'UNISEX', 'Giấy', 'Việt Nam', 'Việt Nam', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', TRUE, FALSE, 149000, 149000, 55, 171, 0, 0, 'bai ma soi ultimate viet hoa boardgame vn boa-0092', 'Bài Ma Sói Ultimate Việt hoá | ToyLand', 'Đồ chơi BoardGame VN chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 5760 MINUTE), DATE_SUB(NOW(), INTERVAL 5760 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bai-ma-soi-ultimate-viet-hoa');
SET @p = (SELECT id FROM products WHERE slug = 'bai-ma-soi-ultimate-viet-hoa');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'BOA-0092' AS sku, '8930000000155' AS barcode, NULL AS option1, NULL AS option2, 149000 AS price, NULL AS compare_at_price, 89000 AS cost_price, 55 AS stock_quantity, 200 AS weight_gram, 'https://placehold.co/600x600/37474F/3E2723/png?text=Bi+Ma+Si+Ultimate+Vi' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/37474F/3E2723/png?text=Bi+Ma+Si+Ultimate+Vit' AS url, 'Bài Ma Sói Ultimate Việt hoá' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Bi+Ma+Si+Ultimate+Vit+2' AS url, 'Bài Ma Sói Ultimate Việt hoá' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Bi+Ma+Si+Ultimate+Vit+3' AS url, 'Bài Ma Sói Ultimate Việt hoá' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Bi+Ma+Si+Ultimate+Vit+4' AS url, 'Bài Ma Sói Ultimate Việt hoá' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số người chơi' AS spec_key, '5 - 30' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Số lá bài' AS spec_key, '78' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 5760 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [54] Cờ tỷ phú Monopoly Junior
SET @cat = (SELECT id FROM categories WHERE slug = 'board-game');
SET @brand = (SELECT id FROM brands WHERE slug = 'hasbro-gaming');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Cờ tỷ phú Monopoly Junior', 'co-ty-phu-monopoly-junior', 'Đồ chơi Hasbro Gaming chính hãng, an toàn cho bé', 'Cờ tỷ phú Monopoly Junior chính hãng Hasbro Gaming. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 60, NULL, 'UNISEX', 'Giấy & nhựa', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Cảnh báo! Có chi tiết nhỏ, có thể gây nghẹt thở. Không thích hợp cho trẻ dưới 3 tuổi.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 390000, 390000, 12, 56, 0, 0, 'co ty phu monopoly junior hasbro gaming has-0093', 'Cờ tỷ phú Monopoly Junior | ToyLand', 'Đồ chơi Hasbro Gaming chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 106560 MINUTE), DATE_SUB(NOW(), INTERVAL 106560 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'co-ty-phu-monopoly-junior');
SET @p = (SELECT id FROM products WHERE slug = 'co-ty-phu-monopoly-junior');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'HAS-0093' AS sku, '8930000000156' AS barcode, NULL AS option1, NULL AS option2, 390000 AS price, NULL AS compare_at_price, 234000 AS cost_price, 12 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/EF5350/3E2723/png?text=C+t+ph+Monopoly+Jun' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/EF5350/3E2723/png?text=C+t+ph+Monopoly+Junio' AS url, 'Cờ tỷ phú Monopoly Junior' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=C+t+ph+Monopoly+Junio+2' AS url, 'Cờ tỷ phú Monopoly Junior' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=C+t+ph+Monopoly+Junio+3' AS url, 'Cờ tỷ phú Monopoly Junior' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=C+t+ph+Monopoly+Junio+4' AS url, 'Cờ tỷ phú Monopoly Junior' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số người chơi' AS spec_key, '2 - 4' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Thời gian chơi' AS spec_key, '20 phút' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 106560 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [55] Bài UNO All Wild
SET @cat = (SELECT id FROM categories WHERE slug = 'board-game');
SET @brand = (SELECT id FROM brands WHERE slug = 'mattel-games');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bài UNO All Wild', 'bai-uno-all-wild', 'Đồ chơi Mattel Games chính hãng, an toàn cho bé', 'Bài UNO All Wild chính hãng Mattel Games. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 84, NULL, 'UNISEX', 'Giấy', 'Mỹ', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 179000, 179000, 40, 103, 0, 0, 'bai uno all wild mattel games mat-0094', 'Bài UNO All Wild | ToyLand', 'Đồ chơi Mattel Games chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 14400 MINUTE), DATE_SUB(NOW(), INTERVAL 14400 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bai-uno-all-wild');
SET @p = (SELECT id FROM products WHERE slug = 'bai-uno-all-wild');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MAT-0094' AS sku, '8930000000157' AS barcode, NULL AS option1, NULL AS option2, 179000 AS price, NULL AS compare_at_price, 107000 AS cost_price, 40 AS stock_quantity, 200 AS weight_gram, 'https://placehold.co/600x600/FF7043/3E2723/png?text=Bi+UNO+All+Wild' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FF7043/3E2723/png?text=Bi+UNO+All+Wild' AS url, 'Bài UNO All Wild' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Bi+UNO+All+Wild+2' AS url, 'Bài UNO All Wild' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Bi+UNO+All+Wild+3' AS url, 'Bài UNO All Wild' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Bi+UNO+All+Wild+4' AS url, 'Bài UNO All Wild' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số người chơi' AS spec_key, '2 - 10' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Số lá bài' AS spec_key, '112' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 14400 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [56] Hộp thẻ Pokémon TCG Booster  (3 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'board-game');
SET @brand = (SELECT id FROM brands WHERE slug = 'pokemon');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Hộp thẻ Pokémon TCG Booster', 'hop-the-pokemon-tcg-booster', 'Đồ chơi Pokémon chính hãng, an toàn cho bé', 'Hộp thẻ Pokémon TCG Booster chính hãng Pokémon. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'UNISEX', 'Giấy', 'Nhật Bản', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Bộ mở rộng', NULL, 'ACTIVE', FALSE, FALSE, 129000, 149000, 54, 32, 0, 0, 'hop the pokemon tcg booster pokemon pokemon pok-0095-01 pok-0095-02 pok-0095-03', 'Hộp thẻ Pokémon TCG Booster | ToyLand', 'Đồ chơi Pokémon chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 2880 MINUTE), DATE_SUB(NOW(), INTERVAL 2880 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'hop-the-pokemon-tcg-booster');
SET @p = (SELECT id FROM products WHERE slug = 'hop-the-pokemon-tcg-booster');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'POK-0095-01' AS sku, '8930000000158' AS barcode, 'Scarlet & Violet 151' AS option1, NULL AS option2, 149000 AS price, NULL AS compare_at_price, 89000 AS cost_price, 30 AS stock_quantity, 30 AS weight_gram, 'https://placehold.co/600x600/FFCA28/3E2723/png?text=Scarlet++Violet+151' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'POK-0095-02' AS sku, '8930000000159' AS barcode, 'Paldea Evolved' AS option1, NULL AS option2, 129000 AS price, NULL AS compare_at_price, 77000 AS cost_price, 24 AS stock_quantity, 30 AS weight_gram, 'https://placehold.co/600x600/FFCA28/3E2723/png?text=Paldea+Evolved' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'POK-0095-03' AS sku, '8930000000160' AS barcode, 'Obsidian Flames' AS option1, NULL AS option2, 129000 AS price, NULL AS compare_at_price, 77000 AS cost_price, 0 AS stock_quantity, 30 AS weight_gram, 'https://placehold.co/600x600/FFCA28/3E2723/png?text=Obsidian+Flames' AS image_url, FALSE AS is_default, 2 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FFCA28/3E2723/png?text=Hp+th+Pokmon+TCG+Boos' AS url, 'Hộp thẻ Pokémon TCG Booster' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Hp+th+Pokmon+TCG+Boos+2' AS url, 'Hộp thẻ Pokémon TCG Booster' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Hp+th+Pokmon+TCG+Boos+3' AS url, 'Hộp thẻ Pokémon TCG Booster' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Hp+th+Pokmon+TCG+Boos+4' AS url, 'Hộp thẻ Pokémon TCG Booster' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số lá / gói' AS spec_key, '10' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT IGNORE INTO product_franchises (product_id, franchise_id) SELECT @p, id FROM franchises WHERE slug = 'pokemon';
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 2880 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [57] Xe chòi chân thăng bằng  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'van-dong-ngoai-troi');
SET @brand = (SELECT id FROM brands WHERE slug = 'micro-kickboard');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Xe chòi chân thăng bằng', 'xe-choi-chan-thang-bang', 'Đồ chơi Micro Kickboard chính hãng, an toàn cho bé', 'Xe thăng bằng giúp bé 2 - 5 tuổi tập giữ thăng bằng trước khi đi xe đạp. Chọn màu và loại bánh: bánh đặc không lo xịt lốp, bánh hơi êm hơn khi đi đường gồ ghề.', @cat, @brand, 24, 60, 'UNISEX', 'Nhôm & nhựa', 'Thụy Sĩ', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 70, 20, 45, 'Màu sắc', 'Bánh xe', 'ACTIVE', TRUE, FALSE, 1590000, 1790000, 16, 148, 0, 0, 'xe choi chan thang bang micro kickboard mic-0096-01 mic-0096-02 mic-0096-03 mic-0096-04', 'Xe chòi chân thăng bằng | ToyLand', 'Đồ chơi Micro Kickboard chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 33120 MINUTE), DATE_SUB(NOW(), INTERVAL 33120 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'xe-choi-chan-thang-bang');
SET @p = (SELECT id FROM products WHERE slug = 'xe-choi-chan-thang-bang');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MIC-0096-01' AS sku, '8930000000161' AS barcode, 'Đỏ' AS option1, 'Bánh đặc' AS option2, 1590000 AS price, NULL AS compare_at_price, 954000 AS cost_price, 6 AS stock_quantity, 3000 AS weight_gram, 'https://placehold.co/600x600/4DB6AC/3E2723/png?text=Bnh+c' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'MIC-0096-02' AS sku, '8930000000162' AS barcode, 'Đỏ' AS option1, 'Bánh hơi' AS option2, 1790000 AS price, NULL AS compare_at_price, 1074000 AS cost_price, 3 AS stock_quantity, 3200 AS weight_gram, 'https://placehold.co/600x600/4DB6AC/3E2723/png?text=Bnh+hi' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'MIC-0096-03' AS sku, '8930000000163' AS barcode, 'Xanh mint' AS option1, 'Bánh đặc' AS option2, 1590000 AS price, NULL AS compare_at_price, 954000 AS cost_price, 7 AS stock_quantity, 3000 AS weight_gram, 'https://placehold.co/600x600/4DB6AC/3E2723/png?text=Xanh+mint+Bnh+c' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'MIC-0096-04' AS sku, '8930000000164' AS barcode, 'Xanh mint' AS option1, 'Bánh hơi' AS option2, 1790000 AS price, NULL AS compare_at_price, 1074000 AS cost_price, 0 AS stock_quantity, 3200 AS weight_gram, 'https://placehold.co/600x600/4DB6AC/3E2723/png?text=Xanh+mint+Bnh+hi' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/4DB6AC/3E2723/png?text=Xe+chi+chn+thng+bng' AS url, 'Xe chòi chân thăng bằng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Xe+chi+chn+thng+bng+2' AS url, 'Xe chòi chân thăng bằng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Xe+chi+chn+thng+bng+3' AS url, 'Xe chòi chân thăng bằng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Xe+chi+chn+thng+bng+4' AS url, 'Xe chòi chân thăng bằng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Tải trọng tối đa' AS spec_key, '30 kg' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Yên' AS spec_key, 'Điều chỉnh 3 mức' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 33120 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [58] Súng nước Nerf Super Soaker  (2 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'van-dong-ngoai-troi');
SET @brand = (SELECT id FROM brands WHERE slug = 'nerf');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Súng nước Nerf Super Soaker', 'sung-nuoc-nerf-super-soaker', 'Đồ chơi Nerf chính hãng, an toàn cho bé', 'Súng nước Nerf Super Soaker chính hãng Nerf. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 72, NULL, 'UNISEX', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Không xịt vào mắt. Chỉ dùng nước sạch.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Mẫu', NULL, 'INACTIVE', FALSE, FALSE, 249000, 349000, 0, 0, 0, 0, 'sung nuoc nerf super soaker nerf ner-0097-01 ner-0097-02', 'Súng nước Nerf Super Soaker | ToyLand', 'Đồ chơi Nerf chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 158400 MINUTE), DATE_SUB(NOW(), INTERVAL 158400 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'sung-nuoc-nerf-super-soaker');
SET @p = (SELECT id FROM products WHERE slug = 'sung-nuoc-nerf-super-soaker');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'NER-0097-01' AS sku, '8930000000165' AS barcode, 'Twister' AS option1, NULL AS option2, 249000 AS price, NULL AS compare_at_price, 149000 AS cost_price, 0 AS stock_quantity, 300 AS weight_gram, 'https://placehold.co/600x600/29B6F6/3E2723/png?text=Twister' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'NER-0097-02' AS sku, '8930000000166' AS barcode, 'Hydra' AS option1, NULL AS option2, 349000 AS price, NULL AS compare_at_price, 209000 AS cost_price, 0 AS stock_quantity, 450 AS weight_gram, 'https://placehold.co/600x600/29B6F6/3E2723/png?text=Hydra' AS image_url, FALSE AS is_default, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/29B6F6/3E2723/png?text=Sng+nc+Nerf+Super+Soa' AS url, 'Súng nước Nerf Super Soaker' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Sng+nc+Nerf+Super+Soa+2' AS url, 'Súng nước Nerf Super Soaker' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Sng+nc+Nerf+Super+Soa+3' AS url, 'Súng nước Nerf Super Soaker' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Sng+nc+Nerf+Super+Soa+4' AS url, 'Súng nước Nerf Super Soaker' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 158400 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [59] Ván trượt skateboard trẻ em  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'van-dong-ngoai-troi');
SET @brand = (SELECT id FROM brands WHERE slug = 'micro-kickboard');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Ván trượt skateboard trẻ em', 'van-truot-skateboard-tre-em', 'Đồ chơi Micro Kickboard chính hãng, an toàn cho bé', 'Ván trượt skateboard trẻ em chính hãng Micro Kickboard. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 60, NULL, 'UNISEX', 'Gỗ phong 7 lớp', 'Thụy Sĩ', 'Trung Quốc', 2025, NULL, FALSE, 'Luôn đội mũ bảo hiểm và đồ bảo hộ khi trượt.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Kích thước', 'Màu sắc', 'ACTIVE', FALSE, FALSE, 590000, 690000, 21, 24, 0, 0, 'van truot skateboard tre em micro kickboard mic-0098-01 mic-0098-02 mic-0098-03 mic-0098-04', 'Ván trượt skateboard trẻ em | ToyLand', 'Đồ chơi Micro Kickboard chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 72000 MINUTE), DATE_SUB(NOW(), INTERVAL 72000 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'van-truot-skateboard-tre-em');
SET @p = (SELECT id FROM products WHERE slug = 'van-truot-skateboard-tre-em');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MIC-0098-01' AS sku, '8930000000167' AS barcode, '60 cm' AS option1, 'Đen' AS option2, 590000 AS price, NULL AS compare_at_price, 354000 AS cost_price, 8 AS stock_quantity, 1800 AS weight_gram, 'https://placehold.co/600x600/263238/3E2723/png?text=60+cm+en' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'MIC-0098-02' AS sku, '8930000000168' AS barcode, '60 cm' AS option1, 'Xanh' AS option2, 590000 AS price, NULL AS compare_at_price, 354000 AS cost_price, 6 AS stock_quantity, 1800 AS weight_gram, 'https://placehold.co/600x600/263238/3E2723/png?text=60+cm+Xanh' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'MIC-0098-03' AS sku, '8930000000169' AS barcode, '70 cm' AS option1, 'Đen' AS option2, 690000 AS price, NULL AS compare_at_price, 414000 AS cost_price, 5 AS stock_quantity, 2200 AS weight_gram, 'https://placehold.co/600x600/263238/3E2723/png?text=70+cm+en' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'MIC-0098-04' AS sku, '8930000000170' AS barcode, '70 cm' AS option1, 'Xanh' AS option2, 690000 AS price, NULL AS compare_at_price, 414000 AS cost_price, 2 AS stock_quantity, 2200 AS weight_gram, 'https://placehold.co/600x600/263238/3E2723/png?text=70+cm+Xanh' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/263238/3E2723/png?text=Vn+trt+skateboard+tr' AS url, 'Ván trượt skateboard trẻ em' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Vn+trt+skateboard+tr+2' AS url, 'Ván trượt skateboard trẻ em' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Vn+trt+skateboard+tr+3' AS url, 'Ván trượt skateboard trẻ em' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Vn+trt+skateboard+tr+4' AS url, 'Ván trượt skateboard trẻ em' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 72000 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [60] Bộ mũ bảo hiểm & bảo hộ trẻ em  (4 biến thể)
SET @cat = (SELECT id FROM categories WHERE slug = 'van-dong-ngoai-troi');
SET @brand = (SELECT id FROM brands WHERE slug = 'micro-kickboard');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Bộ mũ bảo hiểm & bảo hộ trẻ em', 'bo-mu-bao-hiem-bao-ho-tre-em', 'Đồ chơi Micro Kickboard chính hãng, an toàn cho bé', 'Bộ mũ bảo hiểm & bảo hộ trẻ em chính hãng Micro Kickboard. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 36, NULL, 'UNISEX', 'Nhựa PC & mút EPS', 'Thụy Sĩ', 'Trung Quốc', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, 'Size', 'Màu sắc', 'ACTIVE', FALSE, FALSE, 490000, 520000, 34, 152, 0, 0, 'bo mu bao hiem & bao ho tre em micro kickboard mic-0099-01 mic-0099-02 mic-0099-03 mic-0099-04', 'Bộ mũ bảo hiểm & bảo hộ trẻ em | ToyLand', 'Đồ chơi Micro Kickboard chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 72000 MINUTE), DATE_SUB(NOW(), INTERVAL 72000 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'bo-mu-bao-hiem-bao-ho-tre-em');
SET @p = (SELECT id FROM products WHERE slug = 'bo-mu-bao-hiem-bao-ho-tre-em');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'MIC-0099-01' AS sku, '8930000000171' AS barcode, 'S (3 - 6 tuổi)' AS option1, 'Hồng' AS option2, 490000 AS price, NULL AS compare_at_price, 294000 AS cost_price, 10 AS stock_quantity, 600 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=S+3++6+tui+Hng' AS image_url, TRUE AS is_default, 0 AS sort_order
  UNION ALL SELECT 'MIC-0099-02' AS sku, '8930000000172' AS barcode, 'S (3 - 6 tuổi)' AS option1, 'Xanh' AS option2, 490000 AS price, NULL AS compare_at_price, 294000 AS cost_price, 12 AS stock_quantity, 600 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=S+3++6+tui+Xanh' AS image_url, FALSE AS is_default, 1 AS sort_order
  UNION ALL SELECT 'MIC-0099-03' AS sku, '8930000000173' AS barcode, 'M (6 - 12 tuổi)' AS option1, 'Hồng' AS option2, 520000 AS price, NULL AS compare_at_price, 312000 AS cost_price, 5 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=M+6++12+tui+Hng' AS image_url, FALSE AS is_default, 2 AS sort_order
  UNION ALL SELECT 'MIC-0099-04' AS sku, '8930000000174' AS barcode, 'M (6 - 12 tuổi)' AS option1, 'Xanh' AS option2, 520000 AS price, NULL AS compare_at_price, 312000 AS cost_price, 7 AS stock_quantity, 700 AS weight_gram, 'https://placehold.co/600x600/EC407A/3E2723/png?text=M+6++12+tui+Xanh' AS image_url, FALSE AS is_default, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/EC407A/3E2723/png?text=B+m+bo+him++bo+h' AS url, 'Bộ mũ bảo hiểm & bảo hộ trẻ em' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=B+m+bo+him++bo+h++2' AS url, 'Bộ mũ bảo hiểm & bảo hộ trẻ em' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=B+m+bo+him++bo+h++3' AS url, 'Bộ mũ bảo hiểm & bảo hộ trẻ em' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=B+m+bo+him++bo+h++4' AS url, 'Bộ mũ bảo hiểm & bảo hộ trẻ em' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Gồm' AS spec_key, 'Mũ, bó gối, bó khuỷu tay, bao cổ tay' AS spec_value, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 72000 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [61] Lều công chúa kèm 100 bóng
SET @cat = (SELECT id FROM categories WHERE slug = 'van-dong-ngoai-troi');
SET @brand = (SELECT id FROM brands WHERE slug = 'teddy-viet');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Lều công chúa kèm 100 bóng', 'leu-cong-chua-kem-100-bong', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', 'Lều công chúa kèm 100 bóng chính hãng Teddy Việt. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 24, NULL, 'UNISEX', 'Vải polyester', 'Việt Nam', 'Việt Nam', 2025, NULL, FALSE, NULL, TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 40, 40, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 399000, 399000, 16, 23, 0, 0, 'leu cong chua kem 100 bong teddy viet ted-0100', 'Lều công chúa kèm 100 bóng | ToyLand', 'Đồ chơi Teddy Việt chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 53280 MINUTE), DATE_SUB(NOW(), INTERVAL 53280 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'leu-cong-chua-kem-100-bong');
SET @p = (SELECT id FROM products WHERE slug = 'leu-cong-chua-kem-100-bong');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'TED-0100' AS sku, '8930000000175' AS barcode, NULL AS option1, NULL AS option2, 399000 AS price, NULL AS compare_at_price, 239000 AS cost_price, 16 AS stock_quantity, 1800 AS weight_gram, 'https://placehold.co/600x600/F48FB1/3E2723/png?text=Lu+cng+cha+km+100' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/F48FB1/3E2723/png?text=Lu+cng+cha+km+100+b' AS url, 'Lều công chúa kèm 100 bóng' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Lu+cng+cha+km+100+b+2' AS url, 'Lều công chúa kèm 100 bóng' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Lu+cng+cha+km+100+b+3' AS url, 'Lều công chúa kèm 100 bóng' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Lu+cng+cha+km+100+b+4' AS url, 'Lều công chúa kèm 100 bóng' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Kích thước lều' AS spec_key, '105 x 135 cm' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Kèm theo' AS spec_key, '100 bóng nhựa 6 cm' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 53280 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

-- [62] Súng Nerf Elite 2.0 Commander RD-6
SET @cat = (SELECT id FROM categories WHERE slug = 'van-dong-ngoai-troi');
SET @brand = (SELECT id FROM brands WHERE slug = 'nerf');
INSERT INTO products (name, slug, short_description, description, category_id, brand_id, age_min_months, age_max_months, gender, material, brand_origin, made_in, manufacture_year, battery, small_parts_warning, safety_warning, cr_certified, importer, length_cm, width_cm, height_cm, option1_name, option2_name, status, featured, online_exclusive, min_price, max_price, total_stock, sold_count, avg_rating, review_count, search_text, meta_title, meta_description, created_at, updated_at)
SELECT 'Súng Nerf Elite 2.0 Commander RD-6', 'sung-nerf-elite-2-0-commander-rd-6', 'Đồ chơi Nerf chính hãng, an toàn cho bé', 'Súng Nerf Elite 2.0 Commander RD-6 chính hãng Nerf. Sản phẩm đạt chuẩn an toàn đồ chơi trẻ em QCVN 3:2019/BKHCN, giúp bé phát triển trí tưởng tượng, khả năng vận động và tư duy sáng tạo.
Phù hợp làm quà tặng sinh nhật, Trung thu, Tết thiếu nhi.', @cat, @brand, 96, NULL, 'BOY', 'Nhựa ABS', 'Mỹ', 'Trung Quốc', 2025, NULL, TRUE, 'Không bắn vào mắt hoặc mặt. Chỉ sử dụng đạn xốp chính hãng.', TRUE, 'Công ty CP Đồ chơi ToyLand Việt Nam', 30, 20, 10, NULL, NULL, 'ACTIVE', FALSE, FALSE, 399000, 399000, 19, 166, 0, 0, 'sung nerf elite 2.0 commander rd-6 nerf ner-0101', 'Súng Nerf Elite 2.0 Commander RD-6 | ToyLand', 'Đồ chơi Nerf chính hãng, an toàn cho bé', DATE_SUB(NOW(), INTERVAL 89280 MINUTE), DATE_SUB(NOW(), INTERVAL 89280 MINUTE)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE slug = 'sung-nerf-elite-2-0-commander-rd-6');
SET @p = (SELECT id FROM products WHERE slug = 'sung-nerf-elite-2-0-commander-rd-6');
INSERT INTO product_variants (product_id, sku, barcode, option1, option2, price, compare_at_price, cost_price, stock_quantity,
                              weight_gram, image_url, is_default, sort_order)
SELECT @p, t.* FROM (
  SELECT 'NER-0101' AS sku, '8930000000176' AS barcode, NULL AS option1, NULL AS option2, 399000 AS price, NULL AS compare_at_price, 239000 AS cost_price, 19 AS stock_quantity, 600 AS weight_gram, 'https://placehold.co/600x600/FF7043/3E2723/png?text=Sng+Nerf+Elite+20+Co' AS image_url, TRUE AS is_default, 0 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p);
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT @p, t.* FROM (
  SELECT 'https://placehold.co/600x600/FF7043/3E2723/png?text=Sng+Nerf+Elite+20+Comm' AS url, 'Súng Nerf Elite 2.0 Commander RD-6' AS alt, 0 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFFFFF/3E2723/png?text=Sng+Nerf+Elite+20+Comm+2' AS url, 'Súng Nerf Elite 2.0 Commander RD-6' AS alt, 1 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/ECEFF1/3E2723/png?text=Sng+Nerf+Elite+20+Comm+3' AS url, 'Súng Nerf Elite 2.0 Commander RD-6' AS alt, 2 AS sort_order
  UNION ALL SELECT 'https://placehold.co/600x600/FFF8E1/3E2723/png?text=Sng+Nerf+Elite+20+Comm+4' AS url, 'Súng Nerf Elite 2.0 Commander RD-6' AS alt, 3 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p);
INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order)
SELECT @p, t.* FROM (
  SELECT 'Số đạn kèm' AS spec_key, '12' AS spec_value, 0 AS sort_order
  UNION ALL SELECT 'Ổ đạn' AS spec_key, 'Xoay 6 viên' AS spec_value, 1 AS sort_order
) t
WHERE NOT EXISTS (SELECT 1 FROM product_specs WHERE product_id = @p);
INSERT INTO stock_movements (variant_id, sku, type, quantity_change, balance_after, ref_code, note, created_by, created_at)
SELECT v.id, v.sku, 'INITIAL', v.stock_quantity, v.stock_quantity, 'KHOI-TAO', 'Tồn kho ban đầu (dữ liệu mẫu bổ sung)', 'admin', DATE_SUB(NOW(), INTERVAL 89280 MINUTE)
FROM product_variants v WHERE v.product_id = @p AND NOT EXISTS (SELECT 1 FROM stock_movements s WHERE s.variant_id = v.id);

COMMIT;
SET SQL_SAFE_UPDATES = 1;

-- Kiem tra nhanh
SELECT COUNT(*) AS tong_san_pham, SUM(status = 'ACTIVE') AS dang_ban FROM products WHERE deleted = FALSE;
SELECT COUNT(*) AS tong_bien_the FROM product_variants;
