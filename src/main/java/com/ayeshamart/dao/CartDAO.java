package com.ayeshamart.dao;

import com.ayeshamart.model.CartItem;
import com.ayeshamart.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the cart_items table (Phase 4).
 * Every statement is scoped by user_id so a buyer can only ever read or
 * modify their own cart rows, at the database level too. The
 * (user_id, product_id) unique constraint guarantees one row per product.
 */
public class CartDAO {

    private static final String INSERT =
            "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
    private static final String COLUMNS =
            "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, "
                    + "p.name AS product_name, p.category, p.image_url, p.stock_qty, "
                    + "p.price AS unit_price, u.name AS seller_name "
                    + "FROM cart_items ci "
                    + "JOIN products p ON p.id = ci.product_id "
                    + "JOIN users u ON u.id = p.seller_id ";
    private static final String SELECT_BY_USER =
            COLUMNS + "WHERE ci.user_id = ? ORDER BY ci.id";
    private static final String SELECT_BY_USER_AND_PRODUCT =
            COLUMNS + "WHERE ci.user_id = ? AND ci.product_id = ?";
    private static final String UPDATE_QUANTITY =
            "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
    private static final String DELETE =
            "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";

    public CartItem insert(CartItem item) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, item.getUserId());
            statement.setLong(2, item.getProductId());
            statement.setInt(3, item.getQuantity());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getLong(1));
                }
            }
            return item;
        }
    }

    public List<CartItem> findByUserId(long userId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            return findByUserId(connection, userId);
        }
    }

    /** Reads a user's cart lines on a caller-managed (transactional) connection. */
    public List<CartItem> findByUserId(Connection connection, long userId) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapRow(resultSet));
                }
            }
        }
        return items;
    }

    /** Removes every cart row of the user. Used by checkout, scoped by user_id. */
    public boolean clear(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM cart_items WHERE user_id = ?")) {
            statement.setLong(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public Optional<CartItem> findByUserAndProduct(long userId, long productId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_AND_PRODUCT)) {
            statement.setLong(1, userId);
            statement.setLong(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    /** Sets the absolute quantity for one cart row. Scoped to the user. */
    public boolean updateQuantity(long userId, long productId, int quantity) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_QUANTITY)) {
            statement.setInt(1, quantity);
            statement.setLong(2, userId);
            statement.setLong(3, productId);
            return statement.executeUpdate() > 0;
        }
    }

    /** Removes one cart row. Scoped to the user. */
    public boolean delete(long userId, long productId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setLong(1, userId);
            statement.setLong(2, productId);
            return statement.executeUpdate() > 0;
        }
    }

    private CartItem mapRow(ResultSet resultSet) throws SQLException {
        CartItem item = new CartItem();
        item.setId(resultSet.getLong("id"));
        item.setUserId(resultSet.getLong("user_id"));
        item.setProductId(resultSet.getLong("product_id"));
        item.setQuantity(resultSet.getInt("quantity"));
        item.setProductName(resultSet.getString("product_name"));
        item.setCategory(resultSet.getString("category"));
        item.setImageUrl(resultSet.getString("image_url"));
        item.setStockQty(resultSet.getInt("stock_qty"));
        item.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        item.setSellerName(resultSet.getString("seller_name"));
        return item;
    }
}