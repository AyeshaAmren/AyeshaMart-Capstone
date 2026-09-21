package com.ayeshamart.service;

import com.ayeshamart.dao.CartDAO;
import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dto.PaymentDetails;
import com.ayeshamart.dto.ShippingDetails;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.Product;
import com.ayeshamart.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Checkout business logic (Phase 5 + 7).
 *
 * <p>{@link #placeOrder(long, ShippingDetails, PaymentDetails)} converts the
 * buyer's database-backed cart into a PENDING order inside a single database
 * transaction:
 * <ol>
 *   <li>reads the cart rows (server side - nothing is trusted from the browser),</li>
 *   <li>re-reads every product from the database inside the transaction so the
 *       unit price and stock are current,</li>
 *   <li>validates the shipping snapshot and the mock payment method,</li>
 *   <li>stores the shipping address and safe payment metadata with the order,</li>
 *   <li>inserts the order and its line items,</li>
 *   <li>atomically decrements product stock,</li>
 *   <li>clears the buyer's cart,</li>
 *   <li>commits - or rolls the whole thing back so no partial order ever exists.</li>
 * </ol>
 * Every amount (subtotal and total) is calculated on the server and card
 * /UPI details are validated but never persisted.
 */
public class OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public OrderService() {
        this(new OrderDAO(), new CartDAO(), new ProductDAO());
    }

    public OrderService(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    /** Backwards-compatible convenience - a COD order with no snapshot. */
    public Order placeOrder(long buyerId) throws SQLException {
        return placeOrder(buyerId, null, PaymentDetails.cod());
    }

    /**
     * Places the order with its delivery snapshot and mock payment metadata.
     * Card/UPI values are validated here and thrown away - the database only
     * stores method, status and reference.
     */
    public Order placeOrder(long buyerId, ShippingDetails shipping, PaymentDetails payment)
            throws SQLException {
        validateShipping(shipping);
        validatePayment(payment);

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<CartItem> items = cartDAO.findByUserId(connection, buyerId);
                if (items.isEmpty()) {
                    throw new ValidationException("Your cart is empty - add products before checking out");
                }

                BigDecimal total = BigDecimal.ZERO;
                List<PreparedLine> lines = new ArrayList<>();
                for (CartItem item : items) {
                    Product product = productDAO.findById(connection, item.getProductId());
                    if (product == null) {
                        throw new ValidationException("A product in your cart is no longer available");
                    }
                    if (item.getQuantity() <= 0) {
                        throw new ValidationException("Cart quantity must be greater than zero");
                    }
                    if (product.getStockQty() < item.getQuantity()) {
                        throw new ValidationException("Not enough stock for " + product.getName()
                                + " (only " + product.getStockQty() + " left)");
                    }
                    BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    total = total.add(lineTotal);
                    lines.add(new PreparedLine(product, item.getQuantity()));
                }

                Order order = new Order();
                order.setBuyerId(buyerId);
                order.setTotalAmount(total);
                applyShipping(order, shipping);
                applyPayment(order, payment);
                order = orderDAO.insert(connection, order);

                for (PreparedLine line : lines) {
                    if (!productDAO.decreaseStock(connection, line.product.getId(), line.quantity)) {
                        throw new ValidationException("Not enough stock for " + line.product.getName());
                    }
                    OrderItem item = orderDAO.insertItem(connection, order.getId(),
                            line.product.getId(), line.quantity, line.product.getPrice());
                    order.getItems().add(item);
                }

                cartDAO.clear(connection, buyerId);
                connection.commit();
                return order;
            } catch (ValidationException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException("Checkout failed due to an internal error", e);
            }
        }
    }

    private void validateShipping(ShippingDetails shipping) {
        if (shipping == null) {
            return;
        }
        if (!shipping.isValid()) {
            throw new ValidationException(shipping.validate().get(0));
        }
    }

    private void validatePayment(PaymentDetails payment) {
        if (payment == null) {
            throw new ValidationException("Select a payment method");
        }
        String error = payment.validate();
        if (error != null) {
            throw new ValidationException(error);
        }
    }

    private void applyShipping(Order order, ShippingDetails shipping) {
        if (shipping == null) {
            return;
        }
        order.setShippingFullName(shipping.getFullName().trim());
        order.setShippingPhone(shipping.getPhone().trim());
        order.setShippingAddressLine1(shipping.getAddressLine1().trim());
        order.setShippingAddressLine2(blankToNull(shipping.getAddressLine2()));
        order.setShippingCity(shipping.getCity().trim());
        order.setShippingState(shipping.getState().trim());
        order.setShippingPincode(shipping.getPincode().trim());
        order.setShippingLandmark(blankToNull(shipping.getLandmark()));
    }

    private void applyPayment(Order order, PaymentDetails payment) {
        String method = payment.normalizedMethod();
        order.setPaymentMethod(method);
        order.setPaymentStatus(
                PaymentDetails.METHOD_COD.equals(method)
                        ? Order.PAYMENT_STATUS_PENDING
                        : Order.PAYMENT_STATUS_SUCCESS);
        order.setPaymentReference(Order.PAYMENT_PREFIX + referenceToken());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /** Short unique order reference that contains no account or card data. */
    private String referenceToken() {
        StringBuilder token = new StringBuilder();
        String chars = "0123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
        for (int i = 0; i < 8; i++) {
            token.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return token.toString();
    }

    public List<Order> ordersFor(long buyerId) throws SQLException {
        return orderDAO.findByBuyerId(buyerId);
    }

    /**
     * Loads one order only if it belongs to the buyer. Throws so the
     * servlet can map it to 404/403 - a buyer can never read another
     * buyer's order, even by editing the order id in the URL.
     */
    public Order orderForBuyer(long buyerId, long orderId) throws SQLException {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new ValidationException("Order not found");
        }
        if (order.getBuyerId() != buyerId) {
            throw new ValidationException("You can only view your own orders");
        }
        order.setItems(orderDAO.findItemsByOrderId(orderId));
        return order;
    }

    public boolean hasPurchased(long buyerId, long productId) throws SQLException {
        return orderDAO.hasPurchased(buyerId, productId);
    }

    private static final class PreparedLine {
        final Product product;
        final int quantity;

        PreparedLine(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}