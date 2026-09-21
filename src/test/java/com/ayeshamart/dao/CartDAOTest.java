package com.ayeshamart.dao;

import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartDAOTest {

    private static CartDAO cartDAO;
    private static ProductDAO productDAO;
    private static UserDAO userDAO;
    private static long buyer1;
    private static long buyer2;
    private static Product productA;
    private static Product productB;
    private long counter;

    @BeforeAll
    static void init() throws Exception {
        ConnectionManager.setDataSource(TestDb.create("cartdaotest"));
        cartDAO = new CartDAO();
        productDAO = new ProductDAO();
        userDAO = new UserDAO();

        buyer1 = userDAO.create(new User("Buyer One", "cartbuyer1@example.com", "hash", "BUYER")).getId();
        buyer2 = userDAO.create(new User("Buyer Two", "cartbuyer2@example.com", "hash", "BUYER")).getId();
        long seller = userDAO.create(new User("Cart Seller", "cartseller@example.com", "hash", "SELLER")).getId();

        productA = productDAO.create(product(seller, "Cart Product A", "Electronics", new BigDecimal("25.00"), 10));
        productB = productDAO.create(product(seller, "Cart Product B", "Books", new BigDecimal("50.00"), 5));
    }

    private static Product product(long sellerId, String name, String category, BigDecimal price, int stock) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(price);
        product.setStockQty(stock);
        product.setCategory(category);
        return product;
    }

    @AfterAll
    static void cleanup() {
        ConnectionManager.close();
    }

    @BeforeEach
    void cleanupCart() throws Exception {
        try (var conn = ConnectionManager.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM cart_items")) {
            stmt.executeUpdate();
        }
        counter = System.nanoTime();
    }

    private void seedCartRow(long buyerId, long productId, int quantity) throws Exception {
        cartDAO.insert(new CartItem(buyerId, productId, quantity));
    }

    @Test
    void insertThenFindByUserIdJoinsProductAndSellerInfo() throws Exception {
        seedCartRow(buyer1, productA.getId(), 3);

        List<CartItem> items = cartDAO.findByUserId(buyer1);

        boolean found = items.stream().anyMatch(i -> i.getProductId() == productA.getId());
        assertTrue(found);
        CartItem item = items.stream().filter(i -> i.getProductId() == productA.getId()).findFirst().orElseThrow();
        assertEquals(3, item.getQuantity());
        assertEquals("Cart Product A", item.getProductName());
        assertEquals(0, new BigDecimal("25.00").compareTo(item.getUnitPrice()));
        assertEquals("Cart Seller", item.getSellerName());
        assertEquals(0, new BigDecimal("75.00").compareTo(item.getSubtotal()));
    }

    @Test
    void findByUserAndProductReturnsOnlyOwnRow() throws Exception {
        seedCartRow(buyer1, productA.getId(), 2);

        assertTrue(cartDAO.findByUserAndProduct(buyer1, productA.getId()).isPresent());
        assertFalse(cartDAO.findByUserAndProduct(buyer2, productA.getId()).isPresent());
    }

    @Test
    void cartOfOtherUserIsNeverVisible() throws Exception {
        seedCartRow(buyer1, productA.getId(), 1);
        seedCartRow(buyer2, productB.getId(), 1);

        assertTrue(cartDAO.findByUserId(buyer1).stream()
                .noneMatch(i -> i.getUserId() == buyer2));
        assertTrue(cartDAO.findByUserId(buyer1).stream()
                .noneMatch(i -> i.getProductId() == productB.getId()));
    }

    @Test
    void updateQuantityChangesOnlyTargetRow() throws Exception {
        seedCartRow(buyer1, productA.getId(), 1);
        seedCartRow(buyer1, productB.getId(), 1);

        boolean ok = cartDAO.updateQuantity(buyer1, productA.getId(), 4);
        assertTrue(ok);

        List<CartItem> items = cartDAO.findByUserId(buyer1);
        CartItem a = items.stream().filter(i -> i.getProductId() == productA.getId()).findFirst().orElseThrow();
        CartItem b = items.stream().filter(i -> i.getProductId() == productB.getId()).findFirst().orElseThrow();
        assertEquals(4, a.getQuantity());
        assertEquals(1, b.getQuantity());
    }

    @Test
    void updateQuantityForAnotherUserIsIgnored() throws Exception {
        seedCartRow(buyer1, productA.getId(), 1);

        boolean ok = cartDAO.updateQuantity(buyer2, productA.getId(), 9);

        assertFalse(ok);
        assertEquals(1, cartDAO.findByUserAndProduct(buyer1, productA.getId()).orElseThrow().getQuantity());
    }

    @Test
    void deleteRemovesRowAndIsScopedToUser() throws Exception {
        seedCartRow(buyer1, productA.getId(), 1);
        seedCartRow(buyer1, productB.getId(), 1);

        assertFalse(cartDAO.delete(buyer2, productA.getId()));
        assertTrue(cartDAO.delete(buyer1, productA.getId()));

        List<CartItem> items = cartDAO.findByUserId(buyer1);
        assertTrue(items.stream().noneMatch(i -> i.getProductId() == productA.getId()));
        assertTrue(items.stream().anyMatch(i -> i.getProductId() == productB.getId()));
    }

    @Test
    void duplicateInsertOfSameProductViolatesUniqueConstraint() throws Exception {
        seedCartRow(buyer1, productA.getId(), 1);

        assertThrows(Exception.class, () -> cartDAO.insert(new CartItem(buyer1, productA.getId(), 5)));
        assertEquals(1, cartDAO.findByUserAndProduct(buyer1, productA.getId()).orElseThrow().getQuantity());
    }

    @Test
    void emptyCartReturnsEmptyList() throws Exception {
        assertTrue(cartDAO.findByUserId(buyer2).isEmpty());
        assertEquals(Optional.empty(), cartDAO.findByUserAndProduct(buyer2, productB.getId()));
    }
}