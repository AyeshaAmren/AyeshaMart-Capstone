package com.ayeshamart.service;

import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.AdminStats;
import com.ayeshamart.model.AdminUser;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Administrator module business logic (Phase 7).
 *
 * <p>ADMIN-only servlets sit behind AuthFilter, so every method here can
 * assume the caller is an administrator. Order status updates use the same
 * validated lifecycle as the seller module (OrderStatus) but without the
 * per-seller ownership check - an administrator may manage any order.
 */
public class AdminService {

    private final OrderDAO orderDAO;
    private final UserDAO userDAO;
    private final ProductDAO productDAO;

    public AdminService() {
        this(new OrderDAO(), new UserDAO(), new ProductDAO());
    }

    public AdminService(OrderDAO orderDAO, UserDAO userDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
        this.productDAO = productDAO;
    }

    /** Dashboard figures - every value comes from the database. */
    public AdminStats stats() throws SQLException {
        AdminStats stats = new AdminStats();
        stats.setTotalUsers(userDAO.countAll());
        stats.setTotalBuyers(userDAO.countByRole("BUYER"));
        stats.setTotalSellers(userDAO.countByRole("SELLER"));
        stats.setTotalAdmins(userDAO.countByRole("ADMIN"));
        stats.setTotalProducts(productDAO.countAll());
        stats.setTotalOrders(orderDAO.countAll());
        stats.setPendingOrders(orderDAO.countByStatus(OrderStatus.PENDING));
        stats.setConfirmedOrders(orderDAO.countByStatus(OrderStatus.CONFIRMED));
        stats.setShippedOrders(orderDAO.countByStatus(OrderStatus.SHIPPED));
        stats.setDeliveredOrders(orderDAO.countByStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderDAO.countByStatus(OrderStatus.CANCELLED));
        stats.setTotalOrderValue(orderDAO.sumTotalAmount());
        return stats;
    }

    /** User list for Manage Users - password hash removed before it reaches a view. */
    public List<AdminUser> users(String query) throws SQLException {
        List<User> users = userDAO.findAllWithSearch(query);
        List<AdminUser> views = new ArrayList<>();
        for (User user : users) {
            AdminUser view = new AdminUser();
            view.setId(user.getId());
            view.setName(user.getName());
            view.setEmail(user.getEmail());
            view.setRole(user.getRole());
            view.setCreatedAt(user.getCreatedAt());
            views.add(view);
        }
        return views;
    }

    /** All products for moderation, optional search by name/description/category. */
    public List<Product> products(String query) throws SQLException {
        return productDAO.findAllWithSearch(query);
    }

    /**
     * Removes a product listing. Products referenced by orders or carts are
     * soft-removed (stock zeroed) because the foreign keys forbid a hard
     * delete; unreferenced products are deleted. Returns 'removed' or
     * 'unlisted' so the servlet can report exactly what happened.
     */
    public String removeListing(long productId) throws SQLException {
        if (productDAO.findById(productId) == null) {
            throw new ValidationException("Product not found");
        }
        try {
            if (productDAO.deleteById(productId)) {
                return "removed";
            }
            throw new ValidationException("Product not found");
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                productDAO.unlist(productId);
                return "unlisted";
            }
            throw e;
        }
    }

    /** All orders for Manage Orders, optional search by id/buyer/status/method. */
    public List<Order> orders(String query) throws SQLException {
        return orderDAO.findAllWithSearch(query);
    }

    /** One order with its full line items for the admin order details page. */
    public Order orderForAdmin(long orderId) throws SQLException {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new ValidationException("Order not found");
        }
        order.setItems(orderDAO.findItemsByOrderId(orderId));
        return order;
    }

    /**
     * Advances any order through the shared lifecycle (same rules as the
     * seller module) and persists it transactionally. An administrator can
     * manage any order - no seller ownership filter applies.
     */
    public String updateStatus(long orderId, String target) throws SQLException {
        String requested = OrderStatus.normalize(target);
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String current = orderDAO.findStatusById(connection, orderId);
                String next = OrderStatus.transition(current, requested);
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
}