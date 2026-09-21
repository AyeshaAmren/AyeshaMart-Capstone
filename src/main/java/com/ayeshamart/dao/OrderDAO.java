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
            "SELECT o.id, o.buyer_id, o.status, o.total_amount, o.created_at FROM orders o ";
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

    /** Inserts an order with status PENDING on a caller-managed connection. */
    public Order insert(Connection connection, Order order) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO orders (buyer_id, status, total_amount) VALUES (?, 'PENDING', ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, order.getBuyerId());
            statement.setBigDecimal(2, order.getTotalAmount());
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

    private Order mapOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();
        order.setId(resultSet.getLong("id"));
        order.setBuyerId(resultSet.getLong("buyer_id"));
        order.setStatus(resultSet.getString("status"));
        order.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        order.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
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