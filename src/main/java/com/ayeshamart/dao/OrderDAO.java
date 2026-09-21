package com.ayeshamart.dao;

import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.SellerOrderLine;
import com.ayeshamart.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data access for the orders and order_items tables (Phase 5).
 * Orders are written inside a transaction (see OrderService); the insert
 * methods therefore take a Connection owned by the caller so the whole
 * checkout - order + items + stock decrement + cart clear - commits or
 * rolls back together. Reads open their own connections and are always
 * scoped by buyer_id.
 */
public class OrderDAO {

    private static final String ORDER_COLUMNS =
            "SELECT o.id, o.buyer_id, o.status, o.total_amount, o.created_at, "
                    + "o.shipping_full_name, o.shipping_phone, o.shipping_address_line1, "
                    + "o.shipping_address_line2, o.shipping_city, o.shipping_state, "
                    + "o.shipping_pincode, o.shipping_landmark, "
                    + "o.payment_method, o.payment_status, o.payment_reference, "
                    + "u.name AS buyer_name "
                    + "FROM orders o LEFT JOIN users u ON u.id = o.buyer_id ";
    private static final String SELECT_ORDER_BY_ID =
            ORDER_COLUMNS + "WHERE o.id = ?";
    private static final String SELECT_ORDERS_BY_BUYER =
            ORDER_COLUMNS + "WHERE o.buyer_id = ? ORDER BY o.created_at DESC, o.id DESC";

    private static final String ITEM_COLUMNS =
            "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, "
                    + "p.name AS product_name, p.image_url, u.name AS seller_name "
                    + "FROM order_items oi "
                    + "JOIN products p ON p.id = oi.product_id "
                    + "JOIN users u ON u.id = p.seller_id ";
    private static final String SELECT_ITEMS_BY_ORDER =
            ITEM_COLUMNS + "WHERE oi.order_id = ? ORDER BY oi.id";

    private static final String HAS_PURCHASED =
            "SELECT COUNT(*) FROM order_items oi "
                    + "JOIN orders o ON o.id = oi.order_id "
                    + "WHERE o.buyer_id = ? AND oi.product_id = ? AND o.status <> 'CANCELLED'";

    private static final String SELECT_DISTINCT_ORDERS_BY_SELLER =
            "SELECT COUNT(DISTINCT o.id) "
                    + "FROM order_items oi "
                    + "JOIN orders o ON o.id = oi.order_id "
                    + "JOIN products p ON p.id = oi.product_id "
                    + "WHERE p.seller_id = ?";

    private static final String SELECT_DISTINCT_ORDERS_BY_SELLER_AND_STATUS =
            SELECT_DISTINCT_ORDERS_BY_SELLER + " AND o.status = ?";

    /** Incoming orders: order_items of the seller's products, newest first. */
    private static final String SELLER_ORDER_COLUMNS =
            "SELECT oi.id AS item_id, o.id AS order_id, oi.product_id, oi.quantity, oi.unit_price, "
                    + "p.name AS product_name, p.image_url, u.name AS buyer_name, "
                    + "o.created_at, o.status "
                    + "FROM order_items oi "
                    + "JOIN orders o ON o.id = oi.order_id "
                    + "JOIN products p ON p.id = oi.product_id "
                    + "JOIN users u ON u.id = o.buyer_id ";
    private static final String SELECT_INCOMING_BY_SELLER =
            SELLER_ORDER_COLUMNS + "WHERE p.seller_id = ? "
                    + "ORDER BY o.created_at DESC, o.id DESC, oi.id";
    private static final String SELECT_INCOMING_BY_SELLER_AND_ORDER =
            SELLER_ORDER_COLUMNS + "WHERE p.seller_id = ? AND o.id = ? "
                    + "ORDER BY oi.id";

    private static final String HAS_ITEM_FOR_SELLER =
            "SELECT COUNT(*) FROM order_items oi "
                    + "JOIN products p ON p.id = oi.product_id "
                    + "WHERE oi.order_id = ? AND p.seller_id = ?";
    private static final String SELECT_STATUS_BY_ID =
            "SELECT status FROM orders WHERE id = ?";
    private static final String UPDATE_STATUS =
            "UPDATE orders SET status = ? WHERE id = ? AND status = ?";

    private static final String COUNT_ORDERS =
            "SELECT COUNT(*) FROM orders";
    private static final String COUNT_ORDERS_BY_STATUS =
            "SELECT COUNT(*) FROM orders WHERE status = ?";
    private static final String SUM_ORDER_VALUE =
            "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status <> 'CANCELLED'";

    /**
     * Inserts an order with status PENDING plus its delivery snapshot and
     * mock-payment metadata on a caller-managed connection. Shipping and
     * payment columns are written here exactly as they were at checkout.
     */
    public Order insert(Connection connection, Order order) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO orders (buyer_id, status, total_amount, "
                        + "shipping_full_name, shipping_phone, shipping_address_line1, "
                        + "shipping_address_line2, shipping_city, shipping_state, "
                        + "shipping_pincode, shipping_landmark, "
                        + "payment_method, payment_status, payment_reference) "
                        + "VALUES (?, 'PENDING', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, order.getBuyerId());
            statement.setBigDecimal(2, order.getTotalAmount());
            setStringOrNull(statement, 3, order.getShippingFullName());
            setStringOrNull(statement, 4, order.getShippingPhone());
            setStringOrNull(statement, 5, order.getShippingAddressLine1());
            setStringOrNull(statement, 6, order.getShippingAddressLine2());
            setStringOrNull(statement, 7, order.getShippingCity());
            setStringOrNull(statement, 8, order.getShippingState());
            setStringOrNull(statement, 9, order.getShippingPincode());
            setStringOrNull(statement, 10, order.getShippingLandmark());
            setStringOrNull(statement, 11, order.getPaymentMethod());
            setStringOrNull(statement, 12, order.getPaymentStatus());
            setStringOrNull(statement, 13, order.getPaymentReference());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getLong(1));
                }
            }
            order.setStatus("PENDING");
            order.setCreatedAt(LocalDateTime.now());
            return order;
        }
    }

    private void setStringOrNull(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    /** Inserts an order line on a caller-managed connection. */
    public OrderItem insertItem(Connection connection, long orderId, long productId,
                                int quantity, BigDecimal unitPrice) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO order_items (order_id, product_id, quantity, unit_price) "
                        + "VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, orderId);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);
            statement.setBigDecimal(4, unitPrice);
            statement.executeUpdate();
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getLong(1));
                }
            }
            return item;
        }
    }

    public Order findById(long orderId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ORDER_BY_ID)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapOrder(resultSet) : null;
            }
        }
    }

    public List<Order> findByBuyerId(long buyerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ORDERS_BY_BUYER)) {
            statement.setLong(1, buyerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
            }
        }
        return orders;
    }

    public List<OrderItem> findItemsByOrderId(long orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ITEMS_BY_ORDER)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapItem(resultSet));
                }
            }
        }
        return items;
    }

    /**
     * Whether a buyer has purchased (non-cancelled order) the given product.
     * Used to decide review eligibility.
     */
    public boolean hasPurchased(long buyerId, long productId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(HAS_PURCHASED)) {
            statement.setLong(1, buyerId);
            statement.setLong(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    /**
     * Distinct incoming orders that contain at least one product owned by
     * the seller. One order shared with another seller counts once.
     */
    public int countDistinctIncomingOrders(long sellerId) throws SQLException {
        return count(sellerId, SELECT_DISTINCT_ORDERS_BY_SELLER, null);
    }

    public int countDistinctIncomingOrdersByStatus(long sellerId, String status) throws SQLException {
        return count(sellerId, SELECT_DISTINCT_ORDERS_BY_SELLER_AND_STATUS, status);
    }

    private int count(long sellerId, String sql, String status) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sellerId);
            if (status != null) {
                statement.setString(2, status);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /**
     * All in-order lines of the seller's products (across every order),
     * newest orders first. A seller can never see another seller's rows
     * because the join is always filtered by products.seller_id.
     */
    public List<SellerOrderLine> findIncomingBySeller(long sellerId) throws SQLException {
        List<SellerOrderLine> lines = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_INCOMING_BY_SELLER)) {
            statement.setLong(1, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lines.add(mapSellerLine(resultSet));
                }
            }
        }
        return lines;
    }

    /**
     * Only the lines of the seller's products inside one specific order,
     * used by Seller Order Details (an order may contain several sellers,
     * each seller sees just their own items).
     */
    public List<SellerOrderLine> findIncomingBySellerAndOrder(long orderId, long sellerId) throws SQLException {
        List<SellerOrderLine> lines = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_INCOMING_BY_SELLER_AND_ORDER)) {
            statement.setLong(1, sellerId);
            statement.setLong(2, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lines.add(mapSellerLine(resultSet));
                }
            }
        }
        return lines;
    }

    /** Whether an order contains at least one product owned by the seller. */
    public boolean hasItemForSeller(Connection connection, long orderId, long sellerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(HAS_ITEM_FOR_SELLER)) {
            statement.setLong(1, orderId);
            statement.setLong(2, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    /** Reads the current status of an order, or null when it does not exist. */
    public String findStatusById(Connection connection, long orderId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_STATUS_BY_ID)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }
    }

    /**
     * Optimistically advances an order's status, but only when it still has
     * the expected current status (compare-and-set), so two sellers acting
     * on the same order can never skip a step. Runs on the caller's
     * transactional connection.
     */
    public boolean updateStatus(Connection connection, long orderId, String newStatus, String expectedStatus)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {
            statement.setString(1, newStatus);
            statement.setLong(2, orderId);
            statement.setString(3, expectedStatus);
            return statement.executeUpdate() > 0;
        }
    }

    // ------------------------------ admin (Phase 7) ------------------------------

    public int countAll() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_ORDERS)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    public int countByStatus(String status) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_ORDERS_BY_STATUS)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /** Gross value of all non-cancelled orders in the store. */
    public java.math.BigDecimal sumTotalAmount() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SUM_ORDER_VALUE)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal(1);
            }
        }
    }

    /**
     * Every order (with buyer display name) for the Admin Orders page.
     * Optional text search matches order id, buyer name, buyer email,
     * status or payment method. All user input is bound as parameters.
     */
    public List<Order> findAllWithSearch(String query) throws SQLException {
        String q = query == null ? null : query.trim();
        boolean hasQuery = q != null && !q.isEmpty();

        StringBuilder sql = new StringBuilder(ORDER_COLUMNS);
        List<Object> params = new ArrayList<>();
        if (hasQuery) {
            sql.append("WHERE LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? "
                    + "OR UPPER(o.status) LIKE ? OR UPPER(o.payment_method) LIKE ? OR o.id = ?");
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            params.add(like);
            params.add(like);
            params.add(q.toUpperCase(Locale.ROOT));
            params.add(q.toUpperCase(Locale.ROOT));
            try {
                params.add(Long.parseLong(q));
            } catch (NumberFormatException e) {
                params.add(-1L); // cannot match any id
            }
        }
        sql.append(" ORDER BY o.created_at DESC, o.id DESC");

        List<Order> orders = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Long) {
                    statement.setLong(i + 1, (Long) value);
                } else {
                    statement.setString(i + 1, (String) value);
                }
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
            }
        }
        return orders;
    }

    private Order mapOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();
        order.setId(resultSet.getLong("id"));
        order.setBuyerId(resultSet.getLong("buyer_id"));
        order.setStatus(resultSet.getString("status"));
        order.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        order.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
        order.setShippingFullName(resultSet.getString("shipping_full_name"));
        order.setShippingPhone(resultSet.getString("shipping_phone"));
        order.setShippingAddressLine1(resultSet.getString("shipping_address_line1"));
        order.setShippingAddressLine2(resultSet.getString("shipping_address_line2"));
        order.setShippingCity(resultSet.getString("shipping_city"));
        order.setShippingState(resultSet.getString("shipping_state"));
        order.setShippingPincode(resultSet.getString("shipping_pincode"));
        order.setShippingLandmark(resultSet.getString("shipping_landmark"));
        order.setPaymentMethod(resultSet.getString("payment_method"));
        order.setPaymentStatus(resultSet.getString("payment_status"));
        order.setPaymentReference(resultSet.getString("payment_reference"));
        order.setBuyerName(resultSet.getString("buyer_name"));
        return order;
    }

    private OrderItem mapItem(ResultSet resultSet) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(resultSet.getLong("id"));
        item.setOrderId(resultSet.getLong("order_id"));
        item.setProductId(resultSet.getLong("product_id"));
        item.setQuantity(resultSet.getInt("quantity"));
        item.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        item.setProductName(resultSet.getString("product_name"));
        item.setImageUrl(resultSet.getString("image_url"));
        item.setSellerName(resultSet.getString("seller_name"));
        return item;
    }

    private SellerOrderLine mapSellerLine(ResultSet resultSet) throws SQLException {
        SellerOrderLine line = new SellerOrderLine();
        line.setItemId(resultSet.getLong("item_id"));
        line.setOrderId(resultSet.getLong("order_id"));
        line.setProductId(resultSet.getLong("product_id"));
        line.setQuantity(resultSet.getInt("quantity"));
        line.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        line.setProductName(resultSet.getString("product_name"));
        line.setImageUrl(resultSet.getString("image_url"));
        line.setBuyerName(resultSet.getString("buyer_name"));
        line.setOrderDate(resultSet.getObject("created_at", LocalDateTime.class));
        line.setStatus(resultSet.getString("status"));
        return line;
    }
}