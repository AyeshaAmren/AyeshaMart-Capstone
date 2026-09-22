# AyeshaMart - Multi-Seller E-Commerce Web Application

College capstone built with **Java 17, Maven, Tomcat 9, Servlets (javax), JSP/JSTL, H2, HikariCP + JDBC**.

Layered MVC: `Controller -> Service -> DAO -> H2 Database`

## Phases

1. Project setup + MVC skeleton *(done)*
2. H2 database + connectivity *(done)*
3. Users + authentication *(done)*
4. Product CRUD *(done)*
5. Browse / search / filter + cart *(done)*
6. Checkout + orders *(done)*
7. Admin + reviews *(done)*
8. Testing + security + AI chatbot *(current)*
9. Deployment + documentation + viva *(later)*

## Build & run

```bash
mvn test          # run all tests
mvn package       # produces target/ayeshamart.war
```

Deploy `target/ayeshamart.war` to Tomcat 9.x `webapps/` and start Tomcat.
The app is served at `http://localhost:8080/ayeshamart/` (default Tomcat port).

> Note: Tomcat 9 uses the `javax.servlet.*` API. Do NOT deploy on Tomcat 10+ (`jakarta.*`).

## H2 web console (database demo)

Open in the browser:

```
http://localhost:8080/ayeshamart/h2-console
```

Login form values:

| Field    | Value                                                |
|----------|------------------------------------------------------|
| JDBC URL | `jdbc:h2:tcp://localhost:9092/~/AyeshaMart/data/ayeshamart` |
| User Name | `sa`                                               |
| Password | *(empty)*                                             |

Steps to connect:
1. Start Tomcat - the app's `DatabaseListener` starts the H2 TCP server on port
   9092 automatically and seeds the persistent database file
   `<user-home>/AyeshaMart/data/ayeshamart.mv.db` (first run only).
2. Open `http://localhost:8080/ayeshamart/h2-console` in the browser.
3. Leave the login form as:
   | JDBC URL  | `jdbc:h2:tcp://localhost:9092/~/AyeshaMart/data/ayeshamart` |
   | User Name | `sa`                                                          |
   | Password  | *(empty)*                                                     |
4. Click **Connect** - the left panel lists `USERS / PRODUCTS / ORDERS /
   ORDER_ITEMS / CART_ITEMS / REVIEWS`.
5. Run a quick sanity check, e.g.:

```sql
SELECT * FROM users;
SELECT * FROM products;
SELECT * FROM products WHERE price = 500;
SELECT * FROM products WHERE category = 'Electronics';

-- INSERT / UPDATE / DELETE demos
INSERT INTO products (seller_id, name, description, price, stock_qty, category)
VALUES (2, 'Demo Item', 'created from H2 console', 99.00, 5, 'Misc');
UPDATE products SET price = 89.00 WHERE name = 'Demo Item';
DELETE FROM products WHERE name = 'Demo Item';
```

Any change made in the UI (later phases) is visible in the console and vice versa,
because the app and console connect to the same persistent database.

> **Access control (Phase 8):** the console is for **local development /
> capstone demonstration only**. `H2ConsoleGuardFilter` allows loopback /
> localhost requests and returns `403 Forbidden` for anything else.
> - Production: set `AYESHAMART_H2_CONSOLE=false` to disable it entirely.
> - Remote opt-in (not recommended): `AYESHAMART_H2_CONSOLE=true`.
> - Do not expose the H2 TCP port (9092) on a public deployment.

### H2 Console demo queries (Phase 8)

Run the safe, commented example queries for `users`, `products`, `orders`,
`order_items`, `cart_items` and `reviews` (SELECT / INSERT / UPDATE / DELETE + UI-DB sync demo):

```
database/demo-queries.sql
```

The file uses bcrypt hashes for demo users, never stores plaintext passwords
or card/CVV data, always respects foreign keys, and its DELETE statements only
remove the demo rows it creates - never the seeded admin/seller/buyer.

### UI <-> Database synchronization (verified in Phase 8)

- **UI -> Database:** add/update/delete a record in the application and it is
  immediately visible in the H2 console (same database).
- **Database -> UI:** update a safe test record in the H2 console, then refresh
  the app page. Example:

```sql
UPDATE products SET price = 599.00 WHERE id = 1;
```

Then open the product / catalogue page in the browser and the new price appears.

## Database configuration

All settings live in one file: `src/main/resources/db.properties`

```
db.url=jdbc:h2:tcp://localhost:9092/~/AyeshaMart/data/ayeshamart
db.username=sa
db.password=
db.driver=org.h2.Driver
db.pool.size=10
db.h2.tcp.port=9092
db.h2.tcp.allowOthers=true
```

Every value can be overridden by environment variables (`AYESHAMART_DB_URL`, `AYESHAMART_DB_USER`,
`AYESHAMART_DB_PASSWORD`, `H2_TCP_PORT`) - this is how the database location is pointed at a
persistent volume on Railway in Phase 9.

- **Schema**: `src/main/resources/schema.sql` (`CREATE TABLE IF NOT EXISTS` - idempotent).
- **Seed**: `src/main/resources/seed.sql` - runs only when the `users` table is empty, so
  existing persistent data is never overwritten on restart. Passwords are bcrypt hashes.

Seed accounts (login with these in the app):

| Role   | Email                 | Password  |
|--------|-----------------------|-----------|
| ADMIN  | admin@ayeshamart.com  | Admin@123 |
| SELLER | seller@ayeshamart.com | Seller@123 |
| BUYER  | buyer@ayeshamart.com  | Buyer@123  |

## AI shopping assistant chatbot (Phase 8)

A floating chat widget appears on every page (bottom-right).

```
Chat UI  ->  /api/v1/chat  ->  ChatServlet  ->  ChatService  ->  ChatProvider  ->  AI Provider
```

- **Endpoint:** `POST /api/v1/chat` with `{"message":"..."}` returns `{"reply":"..."}`.
- **AI provider:** `com.ayeshamart.chat.OpenAIChatProvider` calls an OpenAI-compatible
  chat-completions API using the JDK `HttpClient`. The API key is read ONLY at runtime:
  | Setting          | Environment variable     | Default                                   |
  |------------------|--------------------------|-------------------------------------------|
  | API key (required)| `AYESHAMART_AI_API_KEY` | - (unset -> FAQ fallback only)            |
  | Endpoint URL     | `AYESHAMART_AI_URL`     | `https://api.openai.com/v1/chat/completions` |
  | Model            | `AYESHAMART_AI_MODEL`   | `gpt-4o-mini`                              |
  | Timeout          | `AYESHAMART_AI_TIMEOUT_MS` | `12000`                                  |
- **Input limit:** 500 characters per message (also enforced with `maxlength` in the UI).
- **Rate limit:** 10 messages/minute/session (rolling window in `ChatService`).
- **Fallback:** `com.ayeshamart.chat.FaqChatProvider` answers offline whenever the AI is
  not configured, times out or returns nothing - the chat never fails.
- **Privacy:** the API key never leaves the server and user messages are never written
  to logs (only anonymous warnings with HTTP status codes).

## Health check endpoint (Phase 8)

`GET /api/v1/health` performs a real `SELECT 1` through the connection pool:

```json
{"service":"ayeshamart","status":"UP","database":"UP","timestamp":"2026-..."}
```

`status` is `UP` when the database answers and `DEGRADED` when it does not.

## Security (Phase 8)

- **SQL injection:** every DAO uses `PreparedStatement`; none concatenate user input.
- **Passwords:** jBCrypt hashes; `AuthService` never stores plaintext.
- **Authorization:** `AuthFilter` guards `/buyer/*`, `/seller/*`, `/admin/*`, `/cart/*`;
  wrong role -> 403, anonymous -> login. Seller ownership is enforced in services
  (`ProductService.findOwnedBySeller`, `SellerService.orderForSeller`).
- **XSS:** all dynamic output is escaped with `<c:out>`.
- **Validation:** server-side in `AuthService`, `ProductService`, `ShippingDetails`,
  `PaymentDetails`, `ReviewService`, `OrderStatus`.
- **Session:** regenerated id on login, 30-min timeout, `HttpOnly` cookie, cookie-only tracking.
- **Headers:** `SecurityHeadersFilter` sets CSP, `X-Frame-Options: DENY`, `nosniff`,
  `Referrer-Policy` and `Permissions-Policy` (H2 console excluded so manual DB access works).
- **Secrets/payments:** no API keys or passwords in source; card/UPI/CVV values are
  validated then discarded - only method/status/reference are stored.

## Connectivity on startup

`com.ayeshamart.listener.DatabaseListener` (ServletContextListener) runs when Tomcat starts:
1. starts the H2 TCP server (server mode, port 9092),
2. builds the HikariCP connection pool,
3. runs `schema.sql` + `seed.sql` idempotently,
4. publishes the DataSource on the ServletContext and in `ConnectionManager`,
5. closes the pool and stops the H2 server when Tomcat shuts down.

DAOs never call `DriverManager`; they get pooled connections from
`com.ayeshamart.util.ConnectionManager` and always use try-with-resources.

## Testing

```bash
mvn test
```

- `HomeServletTest` - MVC skeleton forwards `/home` to the JSP view.
- `DatabaseConnectionTest` - verifies HikariCP -> JDBC -> H2 (server mode) by running `SELECT 1`.
- `AuthFilterTest` - role authorization (anonymous / buyer / seller / admin).
- DAO tests - `UserDAO`, `ProductDAO`, `CartDAO`, `OrderDAO`, `ReviewDAO` against the real schema.
- Service tests - `AuthService`, `ProductService`, `CartService`, `OrderService`,
  `ReviewService`, `SellerService`, `AdminService`.
- `ChatServiceTest` + `OpenAIChatProviderTest` - chatbot validation, rate limit and AI/FAQ fallback.
- `HealthServiceTest` - `SELECT 1` liveness check (UP and DEGRADED states).
- `SecurityHeadersFilterTest` - security response headers are applied (H2 console skipped).
- `EndToEndFlowTest` - full buyer -> seller -> admin journey on an in-memory H2 database.

## Git history

Phase 2:

- `feat: add H2 database schema and seed data` - schema.sql, seed.sql
- `feat: configure HikariCP database connectivity` - listener, config, pool, console, tests, README

Phase 8:

- `feat: implement AI shopping chatbot` - chat providers, ChatService, /api/v1/chat, widget
- `security: harden application security` - headers, HttpOnly cookie, health endpoint, logging
- `feat: add H2 database management and demo queries` - database/demo-queries.sql + docs
- `test: add application tests` - chatbot, provider, health, headers and end-to-end flow tests