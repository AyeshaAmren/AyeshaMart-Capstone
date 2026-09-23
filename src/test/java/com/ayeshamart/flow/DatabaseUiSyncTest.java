package com.ayeshamart.flow;

import com.ayeshamart.dto.ProductForm;
import com.ayeshamart.dto.RegisterRequest;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.service.AuthService;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the H2 web console and the application share ONE database, so any
 * change made in either place is immediately visible in the other.
 *
 * UI  -> DB: a product added through ProductService (what the Seller UI
 *            calls) is found immediately by a plain SQL SELECT, i.e. exactly
 *            the query a user would run in the H2 console.
 * DB  -> UI: after an UPDATE executed with plain JDBC (what the H2 console
 *            editor runs), ProductService - the read path of the buyer
 *            catalogue - returns the new value on the next refresh.
 */
class DatabaseUiSyncTest {

    private HikariDataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = TestDb.create("sync-" + System.nanoTime());
        ConnectionManager.setDataSource(dataSource);
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.close();
    }

    @Test
    void productCreatedThroughTheAppIsVisibleToRawSql() throws Exception {
        User seller = new AuthService().register(
                new RegisterRequest("Sync Seller", "syncseller@example.com", "SyncPass1", "SyncPass1", "SELLER"));

        Product created = new ProductService().create(seller.getId(),
                new ProductForm("Sync Widget", "added from the app", "149.00", "10", "Fiction", null));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT name, price FROM products WHERE id = ?")) {
            statement.setLong(1, created.getId());
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next(), "row created by the app must exist for raw SQL");
                assertEquals("Sync Widget", result.getString("name"));
                assertEquals(0, new BigDecimal("149.00").compareTo(result.getBigDecimal("price")));
            }
        }
    }

    @Test
    void updateRunInH2ConsoleIsVisibleToTheAppImmediately() throws Exception {
        User seller = new AuthService().register(
                new RegisterRequest("Sync Seller 2", "syncseller2@example.com", "SyncPass1", "SyncPass1", "SELLER"));
        Product product = new ProductService().create(seller.getId(),
                new ProductForm("Sync Widget 2", "added from the app", "199.00", "5", "Electronics", null));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE products SET price = 599.00 WHERE id = ?")) {
            statement.setLong(1, product.getId());
            assertEquals(1, statement.executeUpdate(), "UPDATE must hit exactly one row");
        }

        ProductService productService = new ProductService();
        Product refreshed = productService.findDetail(product.getId());
        assertEquals(0, new BigDecimal("599.00").compareTo(refreshed.getPrice()),
                "the app reads the price changed in the H2 console on its next call");

        assertTrue(productService.search("Sync Widget 2", null).stream()
                        .anyMatch(p -> p.getId() == product.getId()
                                && p.getPrice().compareTo(new BigDecimal("599.00")) == 0),
                "the buyer catalogue search shows the updated price");
    }
}