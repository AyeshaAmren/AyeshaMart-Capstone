package com.ayeshamart.service;

import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.SellerOrderLine;
import com.ayeshamart.model.SellerStats;
import com.ayeshamart.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seller module business logic (Phase 6). Every query is scoped by the
 * authenticated seller's id at the database level (JOIN products filtered
 * by products.seller_id), so a seller can never read or change another
 * seller's order information by manipulating an order id in the URL.
 */
public class SellerService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** Valid order lifecycle. Terminal statuses have no outgoing edges. */
    private static final Map<String, List<String>> TRANSITIONS;

    static {
        Map<String, List<String>> transitions = new LinkedHashMap<>();
        transitions.put(STATUS_PENDING, List.of(STATUS_CONFIRMED, STATUS_CANCELLED));
        transitions.put(STATUS_CONFIRMED, List.of(STATUS_SHIPPED, STATUS_CANCELLED));
        transitions.put(STATUS_SHIPPED, List.of(STATUS_DELIVERED));
        transitions.put(STATUS_DELIVERED, List.of());
        transitions.put(STATUS_CANCELLED, List.of());
        TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public SellerService() {
        this(new OrderDAO(), new ProductDAO());
    }

    public SellerService(OrderDAO orderDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    /** Dashboard figures - all counted from the database for this seller only. */
    public SellerStats stats(long sellerId) throws SQLException {
        SellerStats stats = new SellerStats();
        stats.setTotalProducts(productDAO.countBySeller(sellerId));
        stats.setActiveProducts(productDAO.countActiveBySeller(sellerId));
        stats.setTotalIncomingOrders(orderDAO.countDistinctIncomingOrders(sellerId));
        stats.setPendingOrders(orderDAO.countDistinctIncomingOrdersByStatus(sellerId, STATUS_PENDING));
        stats.setConfirmedOrders(orderDAO.countDistinctIncomingOrdersByStatus(sellerId, STATUS_CONFIRMED));
        stats.setShippedOrders(orderDAO.countDistinctIncomingOrdersByStatus(sellerId, STATUS_SHIPPED));
        stats.setDeliveredOrders(orderDAO.countDistinctIncomingOrdersByStatus(sellerId, STATUS_DELIVERED));
        stats.setCancelledOrders(orderDAO.countDistinctIncomingOrdersByStatus(sellerId, STATUS_CANCELLED));
        return stats;
    }

    /** Incoming orders for this seller only, newest first. */
    public List<SellerOrderLine> incomingOrders(long sellerId) throws SQLException {
        return orderDAO.findIncomingBySeller(sellerId);
    }

    /**
     * The seller's lines inside one order. Throws when the order contains no
     * product owned by this seller, so a manipulated order id can neither
     * leak another seller's data nor open an unrelated order.
     */
    public List<SellerOrderLine> orderForSeller(long orderId, long sellerId) throws SQLException {
        List<SellerOrderLine> lines = orderDAO.findIncomingBySellerAndOrder(orderId, sellerId);
        if (lines.isEmpty()) {
            throw new ValidationException("Order not found or it contains none of your products");
        }
        return lines;
    }

    /**
     * Advances the order status through the valid lifecycle and persists it.
     * Runs inside one transaction: first verifies the seller owns at least
     * one product in the order, then re-reads the current status, validates
     * the transition server-side and writes it with compare-and-set so a
     * concurrent change never skips a step. Returns the new (persisted)
     * status.
     */
    public String updateStatus(long orderId, long sellerId, String target) throws SQLException {
        String requested = normalize(target);
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!orderDAO.hasItemForSeller(connection, orderId, sellerId)) {
                    throw new ValidationException("Order not found or it contains none of your products");
                }
                String current = orderDAO.findStatusById(connection, orderId);
                String next = transition(current, requested);
                if (!orderDAO.updateStatus(connection, orderId, next, current)) {
                    throw new ValidationException("Order status changed at the same time; please reload and retry");
                }
                connection.commit();
                return next;
            } catch (ValidationException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException("Could not update order status", e);
            }
        }
    }

    /** Statuses a seller may advance an order to from the given status. */
    public List<String> allowedNextStatuses(String status) {
        if (status == null) {
            return List.of();
        }
        List<String> next = TRANSITIONS.get(status);
        return next == null ? List.of() : next;
    }

    /** Server-side transition check - the source of truth for the workflow. */
    private String transition(String current, String requested) {
        if (current == null) {
            throw new ValidationException("Order not found");
        }
        if (!TRANSITIONS.containsKey(requested)) {
            throw new ValidationException("Unknown status: " + requested);
        }
        if (!TRANSITIONS.containsKey(current)) {
            throw new ValidationException("Order status is final and cannot be changed");
        }
        if (!TRANSITIONS.get(current).contains(requested)) {
            throw new ValidationException("Cannot change order status from " + current + " to " + requested);
        }
        return requested;
    }

    private String normalize(String status) {
        return status == null ? null : status.trim().toUpperCase();
    }
}