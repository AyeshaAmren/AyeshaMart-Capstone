-- ============================================================
-- AyeshaMart - H2 Seed Data (Phase 2)
-- Run ONLY when the users table is empty (see DatabaseInitializer),
-- so existing persistent data is never overwritten.
-- Passwords are bcrypt hashes - NEVER plaintext.
--   admin@ayeshamart.com / Admin@123
--   seller@ayeshamart.com / Seller@123
--   buyer@ayeshamart.com  / Buyer@123
-- ============================================================

-- 1 admin, 1 seller, 1 buyer (fixed ids keep the demo easy to follow)
INSERT INTO users (id, name, email, password_hash, role) VALUES
(1, 'System Admin', 'admin@ayeshamart.com', '$2a$10$qnkeT/caRTJUaPnX460b.ubH/FHNAyZomIP6aGzJHiqgKcsS27miW', 'ADMIN'),
(2, 'Ali Seller',   'seller@ayeshamart.com', '$2a$10$cW64EW0oP4yqNVzKKUCxoOneVLf2ldvaHhOc6CulGqVRsmyOW.UDK', 'SELLER'),
(3, 'Sara Buyer',   'buyer@ayeshamart.com',  '$2a$10$athSH72T3nngDFugfX76VOtFGRRKzlilkkHRuXOVukO9XyAJ.7eX2', 'BUYER');

-- Advance the identity sequence past the explicitly inserted ids,
-- so new rows get ids 4, 5, ... instead of colliding with 1..3.
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;

-- sample products (seller id 2). Two items are exactly 500.00 so the
-- demo query `SELECT * FROM products WHERE price = 500;` returns rows.
INSERT INTO products (seller_id, name, description, price, stock_qty, category) VALUES
(2, 'Wireless Mouse',     'Ergonomic 2.4GHz wireless mouse', 500.00,  25, 'Electronics'),
(2, 'Noise Cancelling Headphones', 'Over-ear Bluetooth headphones', 1500.00,  10, 'Electronics'),
(2, 'USB-C Hub',          '7-in-1 USB-C multiport adapter', 800.00,   30, 'Electronics'),
(2, 'Java Programming',   'Introductory Java textbook for beginners', 500.00, 15, 'Books'),
(2, 'Fiction Thriller',   'Bestselling mystery novel (paperback)', 350.00,  40, 'Books'),
(2, 'Cotton T-Shirt',     '100% cotton unisex t-shirt, size M', 299.00,  50, 'Clothing'),
(2, 'Desk Lamp',          'LED desk lamp with dimmer', 500.00,   20, 'Home'),
(2, 'Running Shoes',      'Lightweight running shoes, size 42', 2200.00, 12, 'Sports');