-- ============================================================
-- AyeshaMart - H2 Database Schema (Phase 2)
-- Idempotent: safe to run on every startup. Existing data is
-- preserved because tables are only created if they do not exist.
-- ============================================================

-- users: buyers, sellers and admins
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(100)  NOT NULL,
    role          VARCHAR(20)   NOT NULL CHECK (role IN ('ADMIN', 'BUYER', 'SELLER')),
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- products: goods offered by sellers
CREATE TABLE IF NOT EXISTS products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id   BIGINT        NOT NULL,
    name        VARCHAR(150)  NOT NULL,
    description VARCHAR(1000),
    price       DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock_qty   INT           NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    category    VARCHAR(100),
    image_url   VARCHAR(500),
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users (id)
);

-- Idempotent migration: adds image_url to databases created before Phase 3
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Recreate dropped index if the ALTER removed it (no-op when already present)
CREATE INDEX IF NOT EXISTS idx_products_seller   ON products (seller_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);

-- orders: buyer purchases with lifecycle status
CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id     BIGINT        NOT NULL,
    status       VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                 CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders (buyer_id);

-- Phase 7: shipping snapshot + mock payment metadata.
-- Shipping is captured ONCE at checkout and stored with the order so the
-- original address remains available even if the buyer's profile changes.
-- Only safe payment metadata is stored - never card numbers, CVV or OTPs.
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_full_name       VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_phone          VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_address_line1  VARCHAR(200);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_address_line2  VARCHAR(200);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_city           VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_state          VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_pincode        VARCHAR(10);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_landmark       VARCHAR(200);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_method          VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_status          VARCHAR(10);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_reference       VARCHAR(50);

ALTER TABLE orders ADD CONSTRAINT IF NOT EXISTS orders_payment_method_check
    CHECK (payment_method IN ('UPI', 'GPAY', 'CARD', 'COD'));
ALTER TABLE orders ADD CONSTRAINT IF NOT EXISTS orders_payment_status_check
    CHECK (payment_status IN ('SUCCESS', 'PENDING', 'FAILED'));

-- order_items: line items inside an order
CREATE TABLE IF NOT EXISTS order_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT        NOT NULL,
    product_id BIGINT        NOT NULL,
    quantity   INT           NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    CONSTRAINT fk_oi_order   FOREIGN KEY (order_id)   REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX IF NOT EXISTS idx_oi_order   ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_oi_product ON order_items (product_id);

-- cart_items: shopping cart lines (persisted per user)
CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL DEFAULT 1 CHECK (quantity > 0),
    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users (id)    ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX IF NOT EXISTS idx_cart_user    ON cart_items (user_id);
CREATE INDEX IF NOT EXISTS idx_cart_product ON cart_items (product_id);

-- Phase 4: a buyer can only ever have ONE row per product.
-- Re-adding a product must increase the existing row quantity instead of
-- creating a second duplicate row (the unique key is also what MERGE needs).
ALTER TABLE cart_items ADD CONSTRAINT IF NOT EXISTS uq_cart_user_product UNIQUE (user_id, product_id);

-- reviews: buyer ratings for products
CREATE TABLE IF NOT EXISTS reviews (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    rating     INT          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    VARCHAR(1000),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)    REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews (product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user    ON reviews (user_id);

-- Phase 5: a buyer may review a product only once.
ALTER TABLE reviews ADD CONSTRAINT IF NOT EXISTS uq_reviews_user_product UNIQUE (user_id, product_id);