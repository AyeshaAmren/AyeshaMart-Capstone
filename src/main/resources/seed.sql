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
(2, 'Java Programming',   'Introductory Java textbook for beginners', 500.00, 15, 'Science'),
(2, 'Fiction Thriller',   'Bestselling mystery novel (paperback)', 350.00,  40, 'Thriller'),
(2, 'Cotton T-Shirt',     '100% cotton unisex t-shirt, size M', 299.00,  50, 'Clothing'),
(2, 'Desk Lamp',          'LED desk lamp with dimmer', 500.00,   20, 'Home'),
(2, 'Running Shoes',      'Lightweight running shoes, size 42', 2200.00, 12, 'Sports');

-- Book catalogue (Phase 4 enrichment): 24 titles across book sub-categories.
-- Image URLs use a deterministic placeholder image service so every book
-- renders a cover in the catalogue without shipping binary assets.
INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
(2, 'The Silent City',        'A city where no one speaks hides the darkest secrets.',         450.00, 18, 'Fiction',     'https://picsum.photos/seed/ayesha-silent-city/300/400'),
(2, 'Whispers of the Past',   'A journalist uncovers letters that rewrite her family history.', 420.00, 15, 'Fiction',     'https://picsum.photos/seed/ayesha-whispers-past/300/400'),
(2, 'The Last Letter',        'A timeless love story told through unmailed letters.',          380.00, 25, 'Fiction',     'https://picsum.photos/seed/ayesha-last-letter/300/400'),
(2, 'A Summer in Tuscany',    'An artist rediscovers life in the hills of Italy.',             460.00, 12, 'Fiction',     'https://picsum.photos/seed/ayesha-summer-tuscany/300/400'),
(2, 'The Midnight Garden',    'A hidden garden blooms only at night, and it remembers everything.',430.00, 20, 'Fiction',     'https://picsum.photos/seed/ayesha-midnight-garden/300/400'),
(2, 'The Silent Witness',     'A mute witness at a murder scene knows more than she can say.',  520.00, 16, 'Thriller',    'https://picsum.photos/seed/ayesha-silent-witness/300/400'),
(2, 'Deadline at Midnight',   'A reporter has one night to break a story someone wants buried.',490.00, 14, 'Thriller',    'https://picsum.photos/seed/ayesha-deadline-midnight/300/400'),
(2, 'The Vanishing Act',      'A magician disappears on stage, only one detective knows how.',  550.00, 11, 'Thriller',    'https://picsum.photos/seed/ayesha-vanishing-act/300/400'),
(2, 'Blood on the Nile',      'A river cruise turns deadly when a passenger goes missing.',     480.00, 19, 'Thriller',    'https://picsum.photos/seed/ayesha-blood-nile/300/400'),
(2, 'The Lantern Murders',    'A lighthouse diary marks the time before each death.',          470.00, 13, 'Mystery',     'https://picsum.photos/seed/ayesha-lantern-murders/300/400'),
(2, 'Shadows of Deception',   'Every clue points to the victim until it does not.',            500.00, 17, 'Mystery',     'https://picsum.photos/seed/ayesha-shadows-deception/300/400'),
(2, 'The Little Cloud That Cried', 'A gentle story about feelings, for little readers.',       260.00, 30, 'Children',    'https://picsum.photos/seed/ayesha-little-cloud/300/400'),
(2, 'Benny the Brave Bear',   'Benny learns that courage is not being unafraid.',              290.00, 28, 'Children',    'https://picsum.photos/seed/ayesha-benny-bear/300/400'),
(2, 'Princess Lila and the Dragon', 'A princess befriends the dragon everyone fears.',         310.00, 26, 'Children',    'https://picsum.photos/seed/ayesha-princess-lila/300/400'),
(2, 'The Secret Treehouse Club', 'Five friends build a summer full of adventures.',           280.00, 32, 'Children',    'https://picsum.photos/seed/ayesha-treehouse-club/300/400'),
(2, 'Max and the Magic Pencil', 'Everything Max draws becomes real, mostly.',                  300.00, 24, 'Children',    'https://picsum.photos/seed/ayesha-max-pencil/300/400'),
(2, 'The Dragon''s Legacy',   'A forgotten guardian must rekindle the fire of the realm.',     620.00, 10, 'Fantasy',     'https://picsum.photos/seed/ayesha-dragon-legacy/300/400'),
(2, 'Realm of the Forgotten', 'A young mage enters a land erased from history.',               600.00, 12, 'Fantasy',     'https://picsum.photos/seed/ayesha-realm-forgotten/300/400'),
(2, 'The Crystal Prophecy',   'A prophecy warns of the crystal''s fall.',                      640.00,  9, 'Fantasy',     'https://picsum.photos/seed/ayesha-crystal-prophecy/300/400'),
(2, 'A Life in Starlight',    'The story of a pioneering female physicist.',                   540.00, 15, 'Biography',   'https://picsum.photos/seed/ayesha-life-starlight/300/400'),
(2, 'Rise Up',                'An athlete''s journey from the streets to the podium.',        510.00, 18, 'Biography',   'https://picsum.photos/seed/ayesha-rise-up/300/400'),
(2, 'The Universe in a Nutshell', 'A clear tour of space, time and everything in between.',     580.00, 14, 'Science',     'https://picsum.photos/seed/ayesha-universe-nutshell/300/400'),
(2, 'How Machines Learn',     'An approachable guide to artificial intelligence.',            560.00, 12, 'Science',     'https://picsum.photos/seed/ayesha-how-machines-learn/300/400'),
(2, 'The Focus Principle',    'A practical system for deep, distraction-free work.',           440.00, 20, 'Self-Help',   'https://picsum.photos/seed/ayesha-focus-principle/300/400');