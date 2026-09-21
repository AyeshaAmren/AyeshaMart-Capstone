package com.ayeshamart.service;

import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.SellerOrderLine;
import com.ayeshamart.model.SellerStats;
import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Seller module tests (Phase 6): dashboard statistics, seller-scoped
 * incoming orders, seller order details and the order status workflow.
 * Mocked tests cover the transition rule table; database-backed tests run
 * the real queries and transactions against the production schema.
 */
@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private OrderDAO orderDAO;
    @Mock
    private ProductDAO productDAO;

    private SellerService sellerService;
    private OrderDAO realOrderDAO;
    private ProductDAO realProductDAO;
    private UserDAO userDAO;
    private long seller1;
    private long seller2;
    private long buyer;
    private long product1;
    private long product2;
    private long product3;

    @BeforeEach
    void setUp() {
        sellerService = new SellerService(orderDAO, productDAO);
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.close();
    }

    private void setUpDatabase() throws Exception {
        String name = "sellersvc-" + System.nanoTime();
        ConnectionManager.setDataSource(TestDb.create(name));
        realOrderDAO = new OrderDAO();
        realProductDAO = new ProductDAO();
        userDAO = new UserDAO();
        seller1 = userDAO.create(new User("Seller One", "s1+" + name + "@example.com", "hash", "SELLER")).getId();
        seller2 = userDAO.create(new User("Seller Two", "s2+" + name + "@example.com", "hash", "SELLER")).getId();
        buyer = userDAO.create(new User("Buyer", "b+" + name + "@example.com", "hash", "BUYER")).getId();

        product1 = product(seller1, "Seller1 Book", 10).getId();
        product2 = product(seller1, "Seller1 Out", 0).getId();
        product3 = product(seller2, "Seller2 Pen", 5).getId();
    }

    private Product product(long sellerId, String name, int stock) throws Exception {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("40.00"));
        product.setStockQty(stock);
        product.setCategory("Books");
        return realProductDAO.create(product);
    }

    /** Seeds an order and returns its id. */
    private long seedOrder(long... productIds) throws Exception {
        Order order = new Order();
        order.setBuyerId(buyer);
        order.setTotalAmount(new BigDecimal("100.00"));
        try (Connection connection = ConnectionManager.getConnection()) {
            order = realOrderDAO.insert(connection, order);
            for (long productId : productIds) {
                realOrderDAO.insertItem(connection, order.getId(), productId, 1, new BigDecimal("40.00"));
            }
        }
        return order.getId();
    }

    private void setStatus(long orderId, String status) throws Exception {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE orders SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, orderId);
            statement.executeUpdate();
        }
    }

    private String statusOf(long orderId) throws Exception {
        try (Connection connection = ConnectionManager.getConnection()) {
            return realOrderDAO.findStatusById(connection, orderId);
        }
    }

    // ---------------------------- unit (transition rules) ----------------------------

    @Test
    void allowedNextStatusesFollowWorkflow() {
        assertEquals(List.of("CONFIRMED", "CANCELLED"), sellerService.allowedNextStatuses("PENDING"));
        assertEquals(List.of("SHIPPED", "CANCELLED"), sellerService.allowedNextStatuses("CONFIRMED"));
        assertEquals(List.of("DELIVERED"), sellerService.allowedNextStatuses("SHIPPED"));
        assertTrue(sellerService.allowedNextStatuses("DELIVERED").isEmpty());
        assertTrue(sellerService.allowedNextStatuses("CANCELLED").isEmpty());
        assertTrue(sellerService.allowedNextStatuses("PENDING").contains("CONFIRMED"));
    }

    @Test
    void statsUsesDaoCounts() throws Exception {
        when(productDAO.countBySeller(7L)).thenReturn(4);
        when(productDAO.countActiveBySeller(7L)).thenReturn(3);
        when(orderDAO.countDistinctIncomingOrders(7L)).thenReturn(6);
        when(orderDAO.countDistinctIncomingOrdersByStatus(7L, "PENDING")).thenReturn(2);
        when(orderDAO.countDistinctIncomingOrdersByStatus(7L, "CONFIRMED")).thenReturn(1);
        when(orderDAO.countDistinctIncomingOrdersByStatus(7L, "SHIPPED")).thenReturn(2);
        when(orderDAO.countDistinctIncomingOrdersByStatus(7L, "DELIVERED")).thenReturn(1);
        when(orderDAO.countDistinctIncomingOrdersByStatus(7L, "CANCELLED")).thenReturn(0);

        SellerStats stats = sellerService.stats(7L);

        assertEquals(4, stats.getTotalProducts());
        assertEquals(3, stats.getActiveProducts());
        assertEquals(6, stats.getTotalIncomingOrders());
        assertEquals(2, stats.getPendingOrders());
        assertEquals(1, stats.getConfirmedOrders());
        assertEquals(2, stats.getShippedOrders());
        assertEquals(1, stats.getDeliveredOrders());
        assertEquals(0, stats.getCancelledOrders());
        verify(productDAO).countBySeller(7L);
    }

    // -------------------------- database integration --------------------------

    @Test
    void statsReflectOnlyThisSellersData() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product1, product3); // both sellers
        long orderB = seedOrder(product2);           // only seller1
        long orderC = seedOrder(product3);           // only seller2
        setStatus(orderB, "DELIVERED");
        setStatus(orderC, "SHIPPED");

        SellerStats stats1 = new SellerService(new OrderDAO(), realProductDAO).stats(seller1);
        SellerStats stats2 = new SellerService(new OrderDAO(), realProductDAO).stats(seller2);

        assertEquals(2, stats1.getTotalProducts());
        assertEquals(1, stats1.getActiveProducts());
        assertEquals(2, stats1.getTotalIncomingOrders());
        assertEquals(1, stats1.getPendingOrders());
        assertEquals(1, stats1.getDeliveredOrders());
        assertEquals(0, stats1.getShippedOrders());
        assertEquals(0, stats1.getCancelledOrders());

        assertEquals(1, stats2.getTotalProducts());
        assertEquals(2, stats2.getTotalIncomingOrders());
        assertEquals(1, stats2.getPendingOrders());
        assertEquals(1, stats2.getShippedOrders());
    }

    @Test
    void incomingOrdersReturnOnlyOwnLines() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product1, product3);
        seedOrder(product2);
        SellerService service = new SellerService(new OrderDAO(), realProductDAO);

        List<SellerOrderLine> lines1 = service.incomingOrders(seller1);
        List<SellerOrderLine> lines2 = service.incomingOrders(seller2);

        assertEquals(2, lines1.size());
        assertTrue(lines1.stream().allMatch(l -> l.getProductId() == product1 || l.getProductId() == product2));
        assertEquals(1, lines2.size());
        assertEquals(product3, lines2.get(0).getProductId());
        assertEquals("Seller2 Pen", lines2.get(0).getProductName());
        assertEquals("Buyer", lines2.get(0).getBuyerName());
    }

    @Test
    void orderForSellerReturnsOnlyOwnItemsFromMixedOrder() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product1, product3);

        List<SellerOrderLine> seller1Lines =
                new SellerService(new OrderDAO(), realProductDAO).orderForSeller(orderA, seller1);
        List<SellerOrderLine> seller2Lines =
                new SellerService(new OrderDAO(), realProductDAO).orderForSeller(orderA, seller2);

        assertEquals(1, seller1Lines.size());
        assertEquals(product1, seller1Lines.get(0).getProductId());
        assertEquals("Seller1 Book", seller1Lines.get(0).getProductName());
        assertEquals(1, seller2Lines.size());
        assertEquals(product3, seller2Lines.get(0).getProductId());
    }

    @Test
    void orderForSellerRejectsOrderWithoutTheirProducts() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product3); // only seller2
        long orderB = seedOrder(product2); // only seller1

        SellerService service = new SellerService(new OrderDAO(), realProductDAO);
        assertThrows(ValidationException.class, () -> service.orderForSeller(orderA, seller1));
        assertThrows(ValidationException.class, () -> service.orderForSeller(orderB, seller2));
    }

    @Test
    void validStatusTransitionsPersistToDatabase() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product1);
        SellerService service = new SellerService(new OrderDAO(), realProductDAO);

        assertEquals("PENDING", statusOf(orderA));
        assertEquals("CONFIRMED", service.updateStatus(orderA, seller1, "CONFIRMED"));
        assertEquals("CONFIRMED", statusOf(orderA));
        assertEquals("SHIPPED", service.updateStatus(orderA, seller1, "SHIPPED"));
        assertEquals("SHIPPED", statusOf(orderA));
        assertEquals("DELIVERED", service.updateStatus(orderA, seller1, "DELIVERED"));
        assertEquals("DELIVERED", statusOf(orderA));
    }

    @Test
    void invalidTransitionsAreRejectedAndNotPersisted() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product1);
        setStatus(orderA, "SHIPPED");
        SellerService service = new SellerService(new OrderDAO(), realProductDAO);

        assertThrows(ValidationException.class, () -> service.updateStatus(orderA, seller1, "CANCELLED"));
        assertEquals("SHIPPED", statusOf(orderA));

        long orderB = seedOrder(product2);
        assertThrows(ValidationException.class, () -> service.updateStatus(orderB, seller1, "DELIVERED"));
        assertEquals("PENDING", statusOf(orderB));
        assertThrows(ValidationException.class, () -> service.updateStatus(orderB, seller1, "BANANAS"));
        assertEquals("PENDING", statusOf(orderB));
    }

    @Test
    void terminalStatusesCannotChange() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product1);
        setStatus(orderA, "DELIVERED");
        long orderB = seedOrder(product2);
        setStatus(orderB, "CANCELLED");
        SellerService service = new SellerService(new OrderDAO(), realProductDAO);

        assertThrows(ValidationException.class, () -> service.updateStatus(orderA, seller1, "CONFIRMED"));
        assertThrows(ValidationException.class, () -> service.updateStatus(orderB, seller1, "CONFIRMED"));
        assertEquals("DELIVERED", statusOf(orderA));
        assertEquals("CANCELLED", statusOf(orderB));
    }

    @Test
    void cancelAllowedFromPendingOrConfirmed() throws Exception {
        setUpDatabase();
        long pendingOrder = seedOrder(product1);
        SellerService service = new SellerService(new OrderDAO(), realProductDAO);

        assertEquals("CANCELLED", service.updateStatus(pendingOrder, seller1, "CANCELLED"));
        assertEquals("CANCELLED", statusOf(pendingOrder));

        long confirmedOrder = seedOrder(product1);
        service.updateStatus(confirmedOrder, seller1, "CONFIRMED");
        assertEquals("CANCELLED", service.updateStatus(confirmedOrder, seller1, "CANCELLED"));
        assertEquals("CANCELLED", statusOf(confirmedOrder));
    }

    @Test
    void sellerCannotUpdateAnotherSellersOrder() throws Exception {
        setUpDatabase();
        long orderA = seedOrder(product3); // only seller2's product
        SellerService service = new SellerService(new OrderDAO(), realProductDAO);

        ValidationException error = assertThrows(ValidationException.class,
                () -> service.updateStatus(orderA, seller1, "CONFIRMED"));
        assertTrue(error.getMessage().contains("not found"));
        assertEquals("PENDING", statusOf(orderA));
    }

    @Test
    void unknownStatusOrOrderAreRejected() throws Exception {
        setUpDatabase();
        SellerService service = new SellerService(new OrderDAO(), realProductDAO);

        ValidationException unknownOrder = assertThrows(ValidationException.class,
                () -> service.updateStatus(987654L, seller1, "CONFIRMED"));
        assertTrue(unknownOrder.getMessage().contains("not found"));

        long orderA = seedOrder(product1);
        ValidationException badStatus = assertThrows(ValidationException.class,
                () -> service.updateStatus(orderA, seller1, null));
        assertTrue(badStatus.getMessage().contains("status"));
    }
}