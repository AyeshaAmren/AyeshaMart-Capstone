# AyeshaMart - Multi-Seller E-Commerce Web Application

College capstone built with **Java 17, Maven, Tomcat 9, Servlets (javax), JSP/JSTL, H2, HikariCP + JDBC**.

Layered MVC: `Controller -> Service -> DAO -> H2 Database`

## Phases

1. Project setup + MVC skeleton
2. H2 database + connectivity *(current)*
3. Users + authentication
4. Product CRUD
5. Browse / search / filter + cart
6. Checkout + orders
7. Admin + reviews
8. Testing + security + AI chatbot
9. Deployment + documentation + viva

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

Steps:
1. Start Tomcat (the app starts the H2 TCP server on port 9092 automatically).
2. Open the console URL above, enter the JDBC URL / user / password.
3. Click **Connect** and run SQL, e.g.:

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

Seed accounts (Phase 3 will add the login form):

| Role   | Email                 | Password  |
|--------|-----------------------|-----------|
| ADMIN  | admin@ayeshamart.com  | Admin@123 |
| SELLER | seller@ayeshamart.com | Seller@123 |
| BUYER  | buyer@ayeshamart.com  | Buyer@123  |

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

## Git history (Phase 2)

- `feat: add H2 database schema and seed data` - schema.sql, seed.sql
- `feat: configure HikariCP database connectivity` - listener, config, pool, console, tests, README