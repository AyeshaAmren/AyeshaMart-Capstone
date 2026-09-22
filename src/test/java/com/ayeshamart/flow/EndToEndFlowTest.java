package com.ayeshamart.flow;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dto.LoginRequest;
import com.ayeshamart.dto.PaymentDetails;
import com.ayeshamart.dto.ProductForm;
import com.ayeshamart.dto.RegisterRequest;
import com.ayeshamart.dto.ShippingDetails;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.AdminStats;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.Review;
import com.ayeshamart.model.User;
import com.ayeshamart.service.AdminService;
import com.ayeshamart.service.AuthService;
import com.ayeshamart.service.CartService;
import com.ayeshamart.service.OrderService;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.service.ReviewService;
import com.ayeshamart.service.SellerService;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 8 end-to-end journey test. Runs the complete Buyer -> Seller ->
 * Administrator workflows against the real production schema (in-memory H2):
 *
 * Buyer:  register -> login -> browse/search -> cart -> checkout -> mock
 *         payment -> order -> review
 * Seller: login -> product create/edit -> incoming order -> status update
 * Admin:  stats -> order details -> status update -> product moderation
 *
 * Every assertion reads back from the database, so it also proves the UI
 * layer's data source (services -> DAO -> H2) stays synchronised with the
 * persisted state.
 */
class EndToEndFlowTest {

    private HikariDataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = TestDb.create("flow-" + System.nanoTime());
        ConnectionManager.setDataSource(dataSource);
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.close();
    }

    @Test
    void completeBuyerSellerAdminJourney() throws Exception {
        User buyer = new AuthService().register(
                new RegisterRequest("Flow Buyer", "flowbuyer@example.com", "FlowPass1", "FlowPass1", "BUYER"));
        User seller = new AuthService().register(
                new RegisterRequest("Flow Seller", "flowseller@example.com", "FlowPass1", "FlowPass1", "SELLER"));

        assertEquals("BUYER", buyer.getRole());
        assertEquals("SELLER", seller.getRole());

        User loggedIn = new AuthService().login(new LoginRequest("flowbuyer@example.com", "FlowPass1"));
        assertEquals(buyer.getId(), loggedIn.getId());

        ProductService productService = new ProductService();
        Product product = productService.create(seller.getId(),
                new ProductForm("Flow Widget", "Flow test widget", "149.00", "20", "Electronics", null));
        long productId = product.getId();
        assertTrue(productId > 0);

        productService.update(seller.getId(), productId,
                new ProductForm("Flow Widget Pro", "Flow test widget v2", "199.00", "15", "Electronics", null));
        Product reloaded = new ProductDAO().findById(productId);
        assertEquals("Flow Widget Pro", reloaded.getName());
        assertEquals(0, new java.math.BigDecimal("199.00").compareTo(reloaded.getPrice()));

        assertEquals(1, productService.findBySeller(seller.getId()).size());
        assertTrue(productService.search("Widget", null).stream()
                .anyMatch(p -> p.getId() == productId));

        CartService cartService = new CartService();
        cartService.add(buyer.getId(), productId, 2);
        assertEquals(2, cartService.cartFor(buyer.getId()).get(0).getQuantity());
        assertEquals(0, new java.math.BigDecimal("398.00").compareTo(cartService.cartTotal(buyer.getId())));

        OrderService orderService = new OrderService();
        Order order = orderService.placeOrder(buyer.getId(),
                new ShippingDetails("Flow Buyer", "9876543210", "1 Flow St", null,
                        "Chennai", "TN", "600001", null),
                new PaymentDetails("UPI", "flowbuyer@okbank", null, null, null, null));

        assertNotNull(order.getId());
        assertEquals(Order.PAYMENT_STATUS_SUCCESS, order.getPaymentStatus());
        assertNotNull(order.getPaymentReference());
        assertTrue(order.getPaymentReference().startsWith(Order.PAYMENT_PREFIX));
        assertTrue(cartService.cartFor(buyer.getId()).isEmpty(), "cart is cleared after checkout");
        assertEquals(13, new ProductDAO().findById(productId).getStockQty(), "stock decremented");

        assertTrue(orderService.ordersFor(buyer.getId()).stream()
                .anyMatch(o -> o.getId() == order.getId()), "buyer sees the order");
        assertThrows(ValidationException.class,
                () -> orderService.orderForBuyer(seller.getId(), order.getId()),
                "a buyer can never read another principal's order");

        ReviewService reviewService = new ReviewService();
        assertTrue(reviewService.canReview(buyer.getId(), productId));
        Review review = reviewService.submit(buyer.getId(), productId, 5, "Excellent product!");
        assertEquals(5, review.getRating());
        assertThrows(ValidationException.class,
                () -> reviewService.submit(buyer.getId(), productId, 4, "duplicate"),
                "one review per product");

        SellerService sellerService = new SellerService();
        assertTrue(sellerService.incomingOrders(seller.getId()).stream()
                .anyMatch(line -> line.getOrderId() == order.getId()));
        assertEquals("CONFIRMED", sellerService.updateStatus(order.getId(), seller.getId(), "CONFIRMED"));
        assertEquals("SHIPPED", sellerService.updateStatus(order.getId(), seller.getId(), "SHIPPED"));

        AdminService adminService = new AdminService();
        AdminStats stats = adminService.stats();
        assertEquals(2, stats.getTotalUsers());
        assertEquals(1, stats.getTotalBuyers());
        assertEquals(1, stats.getTotalSellers());
        assertEquals(1, stats.getTotalProducts());
        assertEquals(1, stats.getTotalOrders());

        Order adminOrder = adminService.orderForAdmin(order.getId());
        assertEquals(1, adminOrder.getItems().size());
        assertEquals("DELIVERED", adminService.updateStatus(order.getId(), "DELIVERED"));

        assertEquals("unlisted", adminService.removeListing(productId),
                "product referenced by an order is soft-removed (stock zeroed), not deleted");
        assertEquals(0, new ProductDAO().findById(productId).getStockQty());
        assertFalse(productService.search("Widget", null).stream()
                        .anyMatch(p -> p.getId() == productId),
                "unlisted product no longer appears in the buyer catalogue");
    }
}