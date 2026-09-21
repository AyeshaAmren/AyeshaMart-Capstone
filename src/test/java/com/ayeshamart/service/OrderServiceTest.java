package com.ayeshamart.service;

import com.ayeshamart.dao.CartDAO;
import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.dto.PaymentDetails;
import com.ayeshamart.dto.ShippingDetails;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.Product;
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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderService tests (Phase 5).
 *
 * <p>The mock-based tests cover order ownership and delegation. The
 * database-backed tests exercise the real transactional checkout against an
 * in-memory H2 loaded from the production schema.sql - they verify the
 * computed totals, PENDING status, stock decrement, cart clearing and,
 * crucially, that a failed order rolls the whole transaction back.</p>
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;
    @Mock
    private CartDAO cartDAO;
    @Mock
    private ProductDAO productDAO;

    private OrderService orderService;
    private UserDAO userDAO;
    private ProductDAO realProductDAO;
    private CartDAO realCartDAO;
    private long buyer;
    private long seller;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderDAO, cartDAO, productDAO);
    }

    private void setUpDatabase(String name) throws Exception {
        ConnectionManager.setDataSource(TestDb.create(name));
        userDAO = new UserDAO();
        realProductDAO = new ProductDAO();
        realCartDAO = new CartDAO();
        seller = userDAO.create(new User("Seller", "seller+" + name + "@example.com", "hash", "SELLER")).getId();
        buyer = userDAO.create(new User("Buyer", "buyer+" + name + "@example.com", "hash", "BUYER")).getId();
    }

    @AfterEach
    void tearDown() {
        orderService = null;
        userDAO = null;
        realProductDAO = null;
        realCartDAO = null;
        ConnectionManager.close();
    }

    private Product product(long sellerId, String name, String price, int stock) throws Exception {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal(price));
        product.setStockQty(stock);
        product.setCategory("Books");
        return realProductDAO.create(product);
    }

    private void addToCart(long buyerId, long productId, int quantity) throws Exception {
        CartItem item = new CartItem(buyerId, productId, quantity);
        realCartDAO.insert(item);
    }

    // ---------------------------- pure unit tests ----------------------------

    @Test
    void ordersForReturnsRepositoryResult() throws Exception {
        Order order = new Order();
        order.setId(5L);
        when(orderDAO.findByBuyerId(1L)).thenReturn(List.of(order));

        List<Order> orders = orderService.ordersFor(1L);

        assertEquals(1, orders.size());
        verify(orderDAO).findByBuyerId(1L);
    }

    @Test
    void hasPurchasedDelegatesToOrderDao() throws Exception {
        when(orderDAO.hasPurchased(1L, 7L)).thenReturn(true);

        assertTrue(orderService.hasPurchased(1L, 7L));
        verify(orderDAO).hasPurchased(1L, 7L);
    }

    @Test
    void orderForBuyerReturnsOwnOrderWithItems() throws Exception {
        Order order = new Order();
        order.setId(4L);
        order.setBuyerId(1L);
        when(orderDAO.findById(4L)).thenReturn(order);
        when(orderDAO.findItemsByOrderId(4L)).thenReturn(List.of(new OrderItem()));

        Order loaded = orderService.orderForBuyer(1L, 4L);

        assertEquals(1, loaded.getItems().size());
        verify(orderDAO).findItemsByOrderId(4L);
    }

    @Test
    void orderForBuyerRejectsUnknownOrder() throws Exception {
        when(orderDAO.findById(999L)).thenReturn(null);

        ValidationException error = assertThrows(ValidationException.class,
                () -> orderService.orderForBuyer(1L, 999L));
        assertEquals("Order not found", error.getMessage());
    }

    @Test
    void orderForBuyerRejectsAnotherBuyersOrder() throws Exception {
        Order order = new Order();
        order.setId(4L);
        order.setBuyerId(99L);
        when(orderDAO.findById(4L)).thenReturn(order);

        ValidationException error = assertThrows(ValidationException.class,
                () -> orderService.orderForBuyer(1L, 4L));
        assertEquals("You can only view your own orders", error.getMessage());
    }

    // -------------------------- transaction tests ----------------------------

    @Test
    void placeOrderCreatesPendingOrderWithServerComputedTotal() throws Exception {
        setUpDatabase("ordersvc-success");
        Product bookA = product(seller, "Book A", "100.00", 10);
        Product bookB = product(seller, "Book B", "50.50", 20);
        addToCart(buyer, bookA.getId(), 2);
        addToCart(buyer, bookB.getId(), 3);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        Order order = service.placeOrder(buyer);

        assertNotNull(order.getId());
        assertEquals("PENDING", order.getStatus());
        assertEquals(0, new BigDecimal("351.50").compareTo(order.getTotalAmount()));
        assertEquals(2, order.getItems().size());

        Order loaded = service.orderForBuyer(buyer, order.getId());
        OrderItem lineA = loaded.getItems().stream()
                .filter(i -> i.getProductId() == bookA.getId()).findFirst().orElseThrow();
        assertEquals(2, lineA.getQuantity());
        assertEquals(0, new BigDecimal("100.00").compareTo(lineA.getUnitPrice()));
        assertEquals("Book A", lineA.getProductName());
        assertEquals("Seller", lineA.getSellerName());

        assertEquals(8, realProductDAO.findById(bookA.getId()).getStockQty());
        assertEquals(17, realProductDAO.findById(bookB.getId()).getStockQty());
        assertTrue(realCartDAO.findByUserId(buyer).isEmpty());
        assertEquals(1, service.ordersFor(buyer).size());
        assertTrue(service.hasPurchased(buyer, bookA.getId()));
        assertFalse(service.hasPurchased(buyer, 424242L));
    }

    @Test
    void insufficientStockRollsBackTheWholeTransaction() throws Exception {
        setUpDatabase("ordersvc-rollback");
        Product book = product(seller, "Rare Book", "10.00", 1);
        addToCart(buyer, book.getId(), 2);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.placeOrder(buyer));
        assertTrue(error.getMessage().contains("stock"));

        assertEquals(1, realProductDAO.findById(book.getId()).getStockQty());
        assertEquals(1, realCartDAO.findByUserId(buyer).size());
        assertTrue(service.ordersFor(buyer).isEmpty());
        assertFalse(service.hasPurchased(buyer, book.getId()));
    }

    @Test
    void emptyCartIsRejected() throws Exception {
        setUpDatabase("ordersvc-empty");
        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);

        ValidationException error = assertThrows(ValidationException.class,
                () -> service.placeOrder(buyer));
        assertTrue(error.getMessage().contains("empty"));
        assertTrue(service.ordersFor(buyer).isEmpty());
    }

    @Test
    void orderForBuyerOnlyReturnsOwnOrders() throws Exception {
        setUpDatabase("ordersvc-scope");
        Product book = product(seller, "Scope Book", "15.00", 5);
        addToCart(buyer, book.getId(), 1);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        Order order = service.placeOrder(buyer);

        long otherBuyer = userDAO.create(
                new User("Other", "other+ordersvc-scope@example.com", "hash", "BUYER")).getId();
        assertNotNull(service.orderForBuyer(buyer, order.getId()));
        assertThrows(ValidationException.class, () -> service.orderForBuyer(otherBuyer, order.getId()));
    }

    // ---------------------- Phase 7: shipping + payment ----------------------

    private ShippingDetails shipping() {
        return new ShippingDetails("Sara Buyer", "9876543210", "12 Main Road",
                "Block C", "Chennai", "Tamil Nadu", "600001", "Near Metro");
    }

    @Test
    void placeOrderStoresShippingSnapshotAndUpiPaymentMetadata() throws Exception {
        setUpDatabase("ordersvc-upi");
        Product book = product(seller, "UPI Book", "80.00", 4);
        addToCart(buyer, book.getId(), 1);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        Order order = service.placeOrder(buyer,
                shipping(), new PaymentDetails("upi", "sara@okbank", null, null, null, null));

        assertEquals("UPI", order.getPaymentMethod());
        assertEquals(Order.PAYMENT_STATUS_SUCCESS, order.getPaymentStatus());
        assertTrue(order.getPaymentReference().startsWith(Order.PAYMENT_PREFIX));

        Order loaded = service.orderForBuyer(buyer, order.getId());
        assertEquals("Sara Buyer", loaded.getShippingFullName());
        assertEquals("9876543210", loaded.getShippingPhone());
        assertEquals("12 Main Road", loaded.getShippingAddressLine1());
        assertEquals("Block C", loaded.getShippingAddressLine2());
        assertEquals("Chennai", loaded.getShippingCity());
        assertEquals("Tamil Nadu", loaded.getShippingState());
        assertEquals("600001", loaded.getShippingPincode());
        assertEquals("Near Metro", loaded.getShippingLandmark());
        assertEquals("UPI", loaded.getPaymentMethod());
        assertEquals(Order.PAYMENT_STATUS_SUCCESS, loaded.getPaymentStatus());
        assertNotNull(loaded.getPaymentReference());
    }

    @Test
    void placeOrderWithCodStartsPaymentPending() throws Exception {
        setUpDatabase("ordersvc-cod");
        Product book = product(seller, "COD Book", "35.00", 3);
        addToCart(buyer, book.getId(), 1);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        Order order = service.placeOrder(buyer, shipping(), PaymentDetails.cod());

        assertEquals("COD", order.getPaymentMethod());
        assertEquals(Order.PAYMENT_STATUS_PENDING, order.getPaymentStatus());
        assertNotNull(order.getPaymentReference());
        assertEquals("COD", service.orderForBuyer(buyer, order.getId()).getPaymentMethod());
    }

    @Test
    void placeOrderWithCardStoresNoSensitiveCardData() throws Exception {
        setUpDatabase("ordersvc-card");
        Product book = product(seller, "Card Book", "60.00", 2);
        addToCart(buyer, book.getId(), 1);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        Order order = service.placeOrder(buyer, shipping(),
                new PaymentDetails("CARD", null, "Sara Buyer", "4111 1111 1111 1111", "12/29", "123"));

        assertEquals("CARD", order.getPaymentMethod());
        assertEquals(Order.PAYMENT_STATUS_SUCCESS, order.getPaymentStatus());

        try (Connection connection = ConnectionManager.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet columns = meta.getColumns(connection.getCatalog(), null, "ORDERS", null)) {
                List<String> names = new java.util.ArrayList<>();
                while (columns.next()) {
                    names.add(columns.getString("COLUMN_NAME").toUpperCase());
                }
                assertFalse(names.contains("CARD_NUMBER"));
                assertFalse(names.contains("CARD_CVV"));
                assertFalse(names.contains("CARD_EXPIRY"));
                assertFalse(names.contains("UPI_ID"));
            }
        }
    }

    @Test
    void placeOrderRejectsInvalidShippingFields() throws Exception {
        setUpDatabase("ordersvc-badshipping");
        Product book = product(seller, "Ship Book", "20.00", 5);
        addToCart(buyer, book.getId(), 1);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        ShippingDetails bad = new ShippingDetails("", "12", "Home", "", "", "", "123", null);

        ValidationException error = assertThrows(ValidationException.class,
                () -> service.placeOrder(buyer, bad, PaymentDetails.cod()));
        assertNotNull(error.getMessage());

        assertEquals(5, realProductDAO.findById(book.getId()).getStockQty());
        assertEquals(1, realCartDAO.findByUserId(buyer).size());
    }

    @Test
    void placeOrderRejectsInvalidPaymentMethodWithoutTouchingStock() throws Exception {
        setUpDatabase("ordersvc-badpayment");
        Product book = product(seller, "Pay Book", "45.00", 6);
        addToCart(buyer, book.getId(), 1);

        OrderService service = new OrderService(new OrderDAO(), realCartDAO, realProductDAO);
        PaymentDetails bad = new PaymentDetails("BITCOIN", null, null, null, null, null);

        ValidationException error = assertThrows(ValidationException.class,
                () -> service.placeOrder(buyer, shipping(), bad));
        assertEquals("Unsupported payment method", error.getMessage());

        assertEquals(6, realProductDAO.findById(book.getId()).getStockQty());
        assertTrue(service.ordersFor(buyer).isEmpty());
    }

    @Test
    void placeOrderRejectsUpiWithInvalidId() throws Exception {
        setUpDatabase("ordersvc-badupi");
        PaymentDetails badUp = new PaymentDetails("GPAY", "not-an-upi-id", null, null, null, null);
        assertEquals("Enter a valid UPI ID (yourid@bankname)", badUp.validate());
    }

    @Test
    void placeOrderRejectsCardWithInvalidDetails() throws Exception {
        setUpDatabase("ordersvc-badcard");
        PaymentDetails noName = new PaymentDetails("CARD", null, " ", "4111111111111111", "12/29", "123");
        assertEquals("Cardholder name is required", noName.validate());

        PaymentDetails badNumber = new PaymentDetails("CARD", null, "Sara", "1234", "12/29", "123");
        assertEquals("Enter a valid card number (12-19 digits)", badNumber.validate());

        PaymentDetails badExpiry = new PaymentDetails("CARD", null, "Sara", "4111111111111111", "13/29", "123");
        assertEquals("Enter a valid expiry date (MM/YY)", badExpiry.validate());

        PaymentDetails badCvv = new PaymentDetails("CARD", null, "Sara", "4111111111111111", "12/29", "12");
        assertEquals("Enter a valid CVV (3-4 digits)", badCvv.validate());
    }
}