package com.ayeshamart.dao;

import com.ayeshamart.model.Review;
import com.ayeshamart.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the reviews table (Phase 5). All SQL uses
 * PreparedStatement. A (user_id, product_id) unique constraint guarantees
 * one review per buyer per product.
 */
public class ReviewDAO {

    private static final String COLUMNS =
            "SELECT r.id, r.product_id, r.user_id, r.rating, r.comment, r.created_at, "
                    + "u.name AS user_name "
                    + "FROM reviews r JOIN users u ON u.id = r.user_id ";
    private static final String SELECT_BY_PRODUCT =
            COLUMNS + "WHERE r.product_id = ? ORDER BY r.created_at DESC, r.id DESC";
    private static final String SELECT_BY_USER_AND_PRODUCT =
            COLUMNS + "WHERE r.user_id = ? AND r.product_id = ?";
    private static final String INSERT =
            "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";

    public Review insert(Review review) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, review.getProductId());
            statement.setLong(2, review.getUserId());
            statement.setInt(3, review.getRating());
            statement.setString(4, review.getComment());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setId(keys.getLong(1));
                }
            }
            review.setCreatedAt(LocalDateTime.now());
            return review;
        }
    }

    public List<Review> findByProductId(long productId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_PRODUCT)) {
            statement.setLong(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapRow(resultSet));
                }
            }
        }
        return reviews;
    }

    public Optional<Review> findByUserAndProduct(long userId, long productId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_AND_PRODUCT)) {
            statement.setLong(1, userId);
            statement.setLong(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    private Review mapRow(ResultSet resultSet) throws SQLException {
        Review review = new Review();
        review.setId(resultSet.getLong("id"));
        review.setProductId(resultSet.getLong("product_id"));
        review.setUserId(resultSet.getLong("user_id"));
        review.setRating(resultSet.getInt("rating"));
        review.setComment(resultSet.getString("comment"));
        review.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
        review.setUserName(resultSet.getString("user_name"));
        return review;
    }
}