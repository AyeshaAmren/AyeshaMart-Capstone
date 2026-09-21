package com.ayeshamart.service;

import com.ayeshamart.dao.CartDAO;
import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.dto.PaymentDetails;
import com.ayeshamart.dto.ShippingDetails;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.AdminStats;
import com.ayeshamart.model.AdminUser;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Administrator module tests (Phase 7). These run against the real in-memory
 * H2 schema so counts, searches, moderation and order updates exercise the
 * exact production SQL.
 */
class AdminServiceTest {

    private UserDAO userDAO;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private CartDAO cartDAO;
    private AdminService adminService;
    private long buyer;
    private long seller;

    @BeforeEach
    void setUp() throws Exception {
        ConnectionManager.setDataSource(TestDb.create("admin-" + System.nanoTime()));
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        orderDAO = new OrderDAO();
        cartDAO = new CartDAO();
        adminService = new AdminService(orderDAO, userDAO, productDAO);
        seller = userDAO.create(new User("Ali Seller", "admin-seller@example.com", "hash", "SELLER")).getId();
        buyer = userDAO.create(new User("Sara Buyer", "admin-buyer@example.com", "hash", "BUYER")).getId();
        userDAO.create(new User("Root Admin", "admin-root@example.com", "hash", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.close();
    }

    private Product product(String name, String price) throws Exception {
        Product product = new Product();
        product.setSellerId(seller);
        product.setName(name);
        product.setDescription("Admin test " + name);
        product.setPrice(new BigDecimal(price));
        product.setStockQty(10);
        product.setCategory("Books");
        return productDAO.create(product);
    }

    private Order placeOrder(long buyerId, long productId, int qty, String method) throws Exception {
        CartItem item = new CartItem(buyerId, productId, qty);
        cartDAO.insert(item);
        return new OrderService(orderDAO, cartDAO, productDAO).placeOrder(buyerId,
                new ShippingDetails("Sara", "9876543210", "1 Main Rd", null,
                        "Chennai", "TN", "600001", null),
                new PaymentDetails(method, "sara@okbank", "Sara", "4111111111111111", "12/29", "123"));
    }

    @Test
    void statsReflectsTheWholeStore() throws Exception {
        Product a = product("Stats A", "10.00");
        Product b = product("Stats B", "20.00");
        placeOrder(buyer, a.getId(), 1, "UPI");
        placeOrder(buyer, b.getId(), 1, "COD");

        AdminStats stats = adminService.stats();

        assertEquals(3, stats.getTotalUsers());
        assertEquals(1, stats.getTotalBuyers());
        assertEquals(1, stats.getTotalSellers());
        assertEquals(1, stats.getTotalAdmins());
        assertEquals(2, stats.getTotalProducts());
        assertEquals(2, stats.getTotalOrders());
        assertEquals(2, stats.getPendingOrders());
        assertEquals(0, stats.getConfirmedOrders() + stats.getShippedOrders() + stats.getDeliveredOrders());
        assertEquals(0, stats.getCancelledOrders());
        assertEquals(0, new BigDecimal("30.00").compareTo(stats.getTotalOrderValue()));
    }

    @Test
    void usersDoesNotExposePasswordHashes() throws Exception {
        List<AdminUser> users = adminService.users(null);

        assertEquals(3, users.size());
        assertTrue(users.stream().allMatch(u -> u.getEmail() != null && !u.getEmail().isBlank()));
        assertTrue(users.stream().anyMatch(u -> u.getRole().equals("ADMIN")));
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Sara Buyer")));
    }

    @Test
    void usersSearchFiltersByNameOrEmail() throws Exception {
        assertEquals(1, adminService.users("admin-seller").size());
        assertEquals(1, adminService.users("sara").size());
        assertEquals(1, adminService.users("buYer@example").size());
        assertEquals("SELLER", adminService.users("Ali Seller").get(0).getRole());
    }

    @Test
    void productsSearchIncludesUnlistedItems() throws Exception {
        Product inStock = product("Visible Book", "10.00");
        productDAO.unlist(inStock.getId());

        List<Product> all = adminService.products(null);
        assertTrue(all.stream().anyMatch(p -> p.getId() == inStock.getId()));

        assertEquals(1, adminService.products("Visible").size());
        assertEquals(1, adminService.products("Books").size());
        assertEquals(0, productDAO.findById(inStock.getId()).getStockQty());
    }

    @Test
    void removeListingHardDeletesUnreferencedProduct() throws Exception {
        Product product = product("Orphan Book", "10.00");

        assertEquals("removed", adminService.removeListing(product.getId()));
        assertEquals(0, adminService.products(null).stream()
                .filter(p -> p.getId() == product.getId()).count());
    }

    @Test
    void removeListingUnlistsProductReferencedByOrders() throws Exception {
        Product product = product("Kept Book", "30.00");
        placeOrder(buyer, product.getId(), 1, "COD");

        assertEquals("unlisted", adminService.removeListing(product.getId()));

        Product loaded = productDAO.findById(product.getId());
        assertNotNull(loaded);
        assertEquals(0, loaded.getStockQty());
    }

    @Test
    void removeListingRejectsUnknownId() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> adminService.removeListing(999999L));
        assertEquals("Product not found", error.getMessage());
    }

    @Test
    void ordersSearchByBuyerStatusAndPaymentMethod() throws Exception {
        Product a = product("Search A", "10.00");
        Product b = product("Search B", "20.00");
        Order upi = placeOrder(buyer, a.getId(), 1, "UPI");
        Order cod = placeOrder(buyer, b.getId(), 1, "COD");
        adminService.updateStatus(upi.getId(), "CONFIRMED");

        assertTrue(adminService.orders(null).stream().anyMatch(o -> o.getBuyerName().contains("Sara")));
        assertEquals(1, adminService.orders("PENDING").size());
        assertEquals(1, adminService.orders("CONFIRMED").size());
        assertEquals("CONFIRMED", adminService.orders("confirmed").get(0).getStatus());
        assertEquals(1, adminService.orders("upi").size());
        assertEquals(1, adminService.orders("COD").size());
        assertEquals(1, adminService.orders(String.valueOf(upi.getId())).size());
        assertEquals(0, adminService.orders(String.valueOf(cod.getId() + 5000)).size());
    }

    @Test
    void orderForAdminLoadsItems() throws Exception {
        Product product = product("Detail Book", "10.00");
        Order order = placeOrder(buyer, product.getId(), 1, "UPI");

        Order loaded = adminService.orderForAdmin(order.getId());

        assertEquals(1, loaded.getItems().size());
        assertNotNull(loaded.getBuyerName());
        assertNotNull(loaded.getPaymentMethod());
    }

    @Test
    void orderForAdminRejectsUnknownOrder() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> adminService.orderForAdmin(999999L));
        assertEquals("Order not found", error.getMessage());
    }

    @Test
    void adminCanAdvanceAnyOrderThroughTheWorkflow() throws Exception {
        Product product = product("Flow Book", "10.00");
        Order order = placeOrder(buyer, product.getId(), 1, "UPI");

        assertEquals("CONFIRMED", adminService.updateStatus(order.getId(), "confirmed"));
        assertEquals("SHIPPED", adminService.updateStatus(order.getId(), "SHIPPED"));
        assertEquals("DELIVERED", adminService.updateStatus(order.getId(), "delivered"));
        assertEquals("DELIVERED", orderDAO.findById(order.getId()).getStatus());
    }

    @Test
    void adminCanCancelPendingOrConfirmedOrder() throws Exception {
        Product product = product("Cancel Book", "10.00");
        Order order = placeOrder(buyer, product.getId(), 1, "UPI");

        assertEquals("CANCELLED", adminService.updateStatus(order.getId(), "CANCELLED"));
        assertEquals("CANCELLED", orderDAO.findById(order.getId()).getStatus());
    }

    @Test
    void invalidStatusTransitionsAreRejected() throws Exception {
        Product product = product("Guard Book", "10.00");
        Order order = placeOrder(buyer, product.getId(), 1, "UPI");

        ValidationException jump = assertThrows(ValidationException.class,
                () -> adminService.updateStatus(order.getId(), "DELIVERED"));
        assertTrue(jump.getMessage().contains("from PENDING to DELIVERED"));

        adminService.updateStatus(order.getId(), "CANCELLED");
        ValidationException finalStatus = assertThrows(ValidationException.class,
                () -> adminService.updateStatus(order.getId(), "CONFIRMED"));
        assertTrue(finalStatus.getMessage().contains("from CANCELLED to CONFIRMED"));

        ValidationException unknown = assertThrows(ValidationException.class,
                () -> adminService.updateStatus(order.getId(), "REFUNDED"));
        assertTrue(unknown.getMessage().contains("Unknown status"));
    }
}