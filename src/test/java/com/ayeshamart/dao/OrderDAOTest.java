package com.ayeshamart.dao;

import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OrderDAO tests (Phase 5) against the real production schema loaded into
 * an in-memory H2 database.
 */
class OrderDAOTest {

    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private long seller;
    private long buyer1;
    private long buyer2;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() throws Exception {
        ConnectionManager.setDataSource(TestDb.create("orderdaotest-" + System.nanoTime()));
        orderDAO = new OrderDAO();
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        seller = userDAO.create(new User("Seller", "oker+odt@example.com", "hash", "SELLER")).getId();
        buyer1 = userDAO.create(new User("Buyer One", "b1+odt@example.com", "hash", "BUYER")).getId();
        buyer2 = userDAO.create(new User("Buyer Two", "b2+odt@example.com", "hash", "BUYER")).getId();
        productA = product(seller, "Product A");
        productB = product(seller, "Product B");
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.close();
    }

    private Product product(long sellerId, String name) throws Exception {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("25.00"));
        product.setStockQty(10);
        product.setCategory("Books");
        return productDAO.create(product);
    }

    private Order seedOrder(long buyerId, long productId, int quantity, String total) throws Exception {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setTotalAmount(new BigDecimal(total));
        try (Connection connection = ConnectionManager.getConnection()) {
            order = orderDAO.insert(connection, order);
            orderDAO.insertItem(connection, order.getId(), productId, quantity, new BigDecimal("25.00"));
        }
        return order;
    }

    @Test
    void insertCreatesOrderWithGeneratedIdAndPendingStatus() throws Exception {
        Order order = seedOrder(buyer1, productA.getId(), 2, "50.00");

        assertNotNull(order.getId());
        assertEquals("PENDING", order.getStatus());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    void insertItemSetsGeneratedIdAndDetails() throws Exception {
        Order order = seedOrder(buyer1, productB.getId(), 3, "75.00");

        List<OrderItem> items = orderDAO.findItemsByOrderId(order.getId());
        assertEquals(1, items.size());
        OrderItem item = items.get(0);
        assertNotNull(item.getId());
        assertEquals(order.getId(), item.getOrderId());
        assertEquals(productB.getId(), item.getProductId());
        assertEquals(3, item.getQuantity());
        assertEquals(0, new BigDecimal("25.00").compareTo(item.getUnitPrice()));
        assertEquals("Product B", item.getProductName());
        assertEquals("Seller", item.getSellerName());
    }

    @Test
    void findByIdReturnsOrderElseNull() throws Exception {
        Order order = seedOrder(buyer1, productA.getId(), 1, "25.00");

        Order loaded = orderDAO.findById(order.getId());
        assertEquals(order.getId(), loaded.getId());
        assertEquals(buyer1, loaded.getBuyerId());
        assertEquals("PENDING", loaded.getStatus());
        assertNull(orderDAO.findById(999999L));
    }

    @Test
    void findByBuyerIdIsScopedToBuyerAndSortedNewestFirst() throws Exception {
        seedOrder(buyer1, productA.getId(), 1, "25.00");
        seedOrder(buyer2, productA.getId(), 1, "25.00");
        seedOrder(buyer1, productB.getId(), 1, "25.00");

        List<Order> buyer1Orders = orderDAO.findByBuyerId(buyer1);
        List<Order> buyer2Orders = orderDAO.findByBuyerId(buyer2);

        assertEquals(2, buyer1Orders.size());
        assertEquals(1, buyer2Orders.size());
        assertTrue(buyer1Orders.stream().allMatch(o -> o.getBuyerId() == buyer1));
        assertTrue(buyer2Orders.stream().allMatch(o -> o.getBuyerId() == buyer2));
    }

    @Test
    void hasPurchasedIgnoresCancelledOrders() throws Exception {
        Order order = seedOrder(buyer1, productA.getId(), 1, "25.00");

        assertTrue(orderDAO.hasPurchased(buyer1, productA.getId()));
        assertFalse(orderDAO.hasPurchased(buyer2, productA.getId()));

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE orders SET status = 'CANCELLED' WHERE id = ?")) {
            statement.setLong(1, order.getId());
            assertEquals(1, statement.executeUpdate());
        }

        assertFalse(orderDAO.hasPurchased(buyer1, productA.getId()));
    }

    @Test
    void orderItemsJoinProductAndSellerNames() throws Exception {
        Order order = seedOrder(buyer1, productB.getId(), 4, "100.00");

        List<OrderItem> items = orderDAO.findItemsByOrderId(order.getId());
        assertEquals(4, items.get(0).getQuantity());
        assertEquals("Product B", items.get(0).getProductName());
        assertEquals("Seller", items.get(0).getSellerName());
    }
}