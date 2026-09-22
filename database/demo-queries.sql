-- ============================================================
-- AyeshaMart - H2 Console demo queries (Phase 8)
-- ============================================================
-- How to open the H2 console:
--   1. Start Tomcat (the app starts the H2 TCP server on port 9092).
--   2. Browse to   http://localhost:8080/ayeshamart/h2-console
--   3. Connect with:
--        JDBC URL : jdbc:h2:tcp://localhost:9092/~/AyeshaMart/data/ayeshamart
--        User Name: sa
--        Password : (empty)
--
-- This file contains SAFE example queries for the demo tables.
-- Notes:
--   * Passwords are ALWAYS bcrypt hashes - never plaintext.
--   * No payment/card/CVV data exists anywhere; only method, status and
--     a payment reference are stored on orders.
--   * Foreign keys are respected (order_items need an order, cart_items/
--     reviews need a real user + product, etc.).
--   * DELETE examples below only touch the demo rows created in THIS file,
--     never the seeded admin/seller/buyer (ids 1-3) - so nothing breaks.
--
-- Tables: users | products | orders | order_items | cart_items | reviews
-- ============================================================


-- ============================================================
-- 1) USERS  (bcrypt hash of "Demo@123" - never store plaintext)
-- ============================================================

-- SELECT - view all accounts (passwords visible as hashes only)
SELECT * FROM users;

-- SELECT - one account
SELECT id, name, email, role, created_at FROM users WHERE email = 'demo@example.com';

-- INSERT - a demo buyer (bcrypt hash of "Demo@123").
-- Deleting it later is safe and has no foreign-key children.
INSERT INTO users (name, email, password_hash, role) VALUES
('Demo Buyer', 'demo@example.com', '$2a$10$PGDvIg4b4xhtg0SHHtYhdOHqj3HFJTYDEdqMLrQOrcaJThpU2uS76', 'BUYER');

-- UPDATE - rename an account. Always filter by email/id, never "all rows".
UPDATE users SET name = 'Demo Buyer Updated' WHERE email = 'demo@example.com';

-- DELETE - remove the demo account created above (safe: no child rows yet).
DELETE FROM users WHERE email = 'demo@example.com';

-- WARNING: never run a DELETE without a WHERE on the seeded accounts
-- (admin id 1, seller id 2, buyer id 3) - the app depends on them.


-- ============================================================
-- 2) PRODUCTS  (full CRUD)
-- ============================================================

-- SELECT - all products / by category / by price
SELECT * FROM products;
SELECT id, name, price, stock_qty, category FROM products WHERE category = 'Electronics';
SELECT * FROM products WHERE price = 500;

-- INSERT - a demo product from the seeded seller (id 2).
INSERT INTO products (seller_id, name, description, price, stock_qty, category)
VALUES (2, 'Demo Widget', 'Created from the H2 console', 199.00, 5, 'Misc');

-- UPDATE - change price / stock. NOTE: see section 7 for the UI <-> DB
-- synchronization demo (UPDATE ... WHERE id = 1).
UPDATE products SET price = 249.00 WHERE name = 'Demo Widget';
UPDATE products SET stock_qty = stock_qty - 1 WHERE id = 8;

-- DELETE - remove the demo product (no order/cart references it yet).
DELETE FROM products WHERE name = 'Demo Widget';

-- WARNING: products referenced by order_items or cart_items cannot be hard
-- deleted (foreign keys). The application unlists them instead (stock -> 0).


-- ============================================================
-- 3) ORDERS  (full CRUD)
-- ============================================================

-- SELECT - all orders / by buyer / by status
SELECT * FROM orders;
SELECT * FROM orders WHERE status = 'PENDING';
SELECT * FROM orders WHERE buyer_id = 3;

-- INSERT - a demo order. The buyer id comes from a subquery so the foreign
-- key is always valid (here: the seeded buyer, email buyer@ayeshamart.com).
INSERT INTO orders (buyer_id, status, total_amount)
SELECT id, 'PENDING', 599.00 FROM users WHERE email = 'buyer@ayeshamart.com';

-- UPDATE - advance a status (allowed: PENDING->CONFIRMED->SHIPPED->DELIVERED,
-- or PENDING/CONFIRMED -> CANCELLED). Filter by buyer/status, not all rows.
UPDATE orders SET status = 'CONFIRMED'
WHERE buyer_id = (SELECT id FROM users WHERE email = 'buyer@ayeshamart.com')
  AND status = 'PENDING';

-- DELETE - remove a demo order and its line items (cascade). Only touch
-- orders you created for testing - never all of them.
DELETE FROM orders
WHERE buyer_id = (SELECT id FROM users WHERE email = 'buyer@ayeshamart.com')
  AND total_amount = 599.00;


-- ============================================================
-- 4) ORDER_ITEMS  (line items belong to one order)
-- ============================================================

-- SELECT
SELECT * FROM order_items;
SELECT oi.* FROM order_items oi JOIN orders o ON o.id = oi.order_id ORDER BY o.id;

-- INSERT - add a line to an existing order, using subqueries so both
-- foreign keys (order_id, product_id) stay valid.
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, (SELECT id FROM products WHERE name = 'Wireless Mouse'), 1, 500.00
FROM orders o
WHERE o.buyer_id = (SELECT id FROM users WHERE email = 'buyer@ayeshamart.com')
  AND o.status = 'PENDING';

-- UPDATE
UPDATE order_items SET quantity = 2
WHERE order_id = (SELECT id FROM orders WHERE buyer_id =
                  (SELECT id FROM users WHERE email = 'buyer@ayeshamart.com'))
  AND product_id = (SELECT id FROM products WHERE name = 'Wireless Mouse');

-- DELETE - line items are deleted automatically when their order is deleted
-- (ON DELETE CASCADE); this removes just one line.
DELETE FROM order_items
WHERE order_id = (SELECT id FROM orders WHERE buyer_id =
                  (SELECT id FROM users WHERE email = 'buyer@ayeshamart.com'));


-- ============================================================
-- 5) CART_ITEMS  (one row per user+product)
-- ============================================================

-- SELECT
SELECT * FROM cart_items;

-- INSERT - a demo cart row for the seeded buyer (id 3) and product (id 1).
INSERT INTO cart_items (user_id, product_id, quantity)
SELECT (SELECT id FROM users  WHERE email = 'buyer@ayeshamart.com'),
       (SELECT id FROM products WHERE name = 'Wireless Mouse'),
       2;

-- UPDATE - change the quantity of a cart row
UPDATE cart_items SET quantity = 3
WHERE user_id    = (SELECT id FROM users   WHERE email = 'buyer@ayeshamart.com')
  AND product_id = (SELECT id FROM products WHERE name = 'Wireless Mouse');

-- DELETE - empty the buyer's cart (or just that row)
DELETE FROM cart_items
WHERE user_id = (SELECT id FROM users WHERE email = 'buyer@ayeshamart.com');


-- ============================================================
-- 6) REVIEWS  (buyer rate a product they purchased)
-- ============================================================

-- SELECT
SELECT * FROM reviews;
SELECT r.*, p.name AS product FROM reviews r JOIN products p ON p.id = r.product_id;

-- INSERT - a 4-star review from the seeded buyer on the seeded product.
INSERT INTO reviews (product_id, user_id, rating, comment)
SELECT (SELECT id FROM products WHERE name = 'Wireless Mouse'),
       (SELECT id FROM users   WHERE email = 'buyer@ayeshamart.com'),
       4,
       'Good demo product, fast delivery.';

-- UPDATE - bump the rating
UPDATE reviews SET rating = 5, comment = 'Updated: very happy with it.'
WHERE product_id = (SELECT id FROM products WHERE name = 'Wireless Mouse')
  AND user_id    = (SELECT id FROM users   WHERE email = 'buyer@ayeshamart.com');

-- DELETE - only the row you created, not every review
DELETE FROM reviews
WHERE product_id = (SELECT id FROM products WHERE name = 'Wireless Mouse');


-- ============================================================
-- 7) UI <-> DATABASE SYNCHRONIZATION DEMO
-- ============================================================
-- The app and the H2 console share the SAME database, so:

-- (a) UI -> Database: add/update/delete a product in the app
--     (Seller module) and verify it appears here with the same price.

-- (b) Database -> UI: run the update below, then open the product page in
--     the browser and refresh - the new price must be visible.
--     (id 1 is the seeded "Wireless Mouse" in a fresh database.)
UPDATE products SET price = 599.00 WHERE id = 1;

-- Show the changed row, then revert or keep as you like.
SELECT id, name, price FROM products WHERE id = 1;
UPDATE products SET price = 500.00 WHERE id = 1;  -- optional revert

-- ============================================================
-- Resources
-- ============================================================
-- Schema:   src/main/resources/schema.sql
-- Seed:     src/main/resources/seed.sql
-- Config:   src/main/resources/db.properties
-- README:   H2 Web Console section (connection procedure + JDBC URL)