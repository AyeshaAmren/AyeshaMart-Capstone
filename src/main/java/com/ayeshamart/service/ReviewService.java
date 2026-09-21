package com.ayeshamart.service;

import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ReviewDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Review;

import java.sql.SQLException;
import java.util.List;

/**
 * Review business logic (Phase 5). Eligibility rules:
 * <ul>
 *   <li>rating must be an integer between 1 and 5,</li>
 *   <li>comment length must be within a reasonable limit (<= 1000),</li>
 *   <li>the reviewer must have purchased the product in a non-cancelled order,</li>
 *   <li>one review per buyer per product (duplicate submissions are rejected).</li>
 * </ul>
 */
public class ReviewService {

    public static final int MAX_COMMENT_LENGTH = 1000;

    private final ReviewDAO reviewDAO;
    private final OrderDAO orderDAO;

    public ReviewService() {
        this(new ReviewDAO(), new OrderDAO());
    }

    public ReviewService(ReviewDAO reviewDAO, OrderDAO orderDAO) {
        this.reviewDAO = reviewDAO;
        this.orderDAO = orderDAO;
    }

    public Review submit(long userId, long productId, int rating, String comment) throws SQLException {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
        String cleaned = comment == null ? null : comment.trim();
        if (cleaned != null && cleaned.length() > MAX_COMMENT_LENGTH) {
            throw new ValidationException("Comment must be at most " + MAX_COMMENT_LENGTH + " characters");
        }
        if (productId <= 0) {
            throw new ValidationException("Product not found");
        }
        if (!orderDAO.hasPurchased(userId, productId)) {
            throw new ValidationException("You can only review products you have purchased");
        }
        if (reviewDAO.findByUserAndProduct(userId, productId).isPresent()) {
            throw new ValidationException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setComment(cleaned == null || cleaned.isEmpty() ? null : cleaned);
        return insert(review, userId, productId);
    }

    private Review insert(Review review, long userId, long productId) throws SQLException {
        try {
            return reviewDAO.insert(review);
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw new ValidationException("You have already reviewed this product");
            }
            throw e;
        }
    }

    private boolean isUniqueViolation(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }

    public List<Review> reviewsFor(long productId) throws SQLException {
        return reviewDAO.findByProductId(productId);
    }

    /** Whether the buyer has already reviewed the product. */
    public boolean hasReviewed(long userId, long productId) throws SQLException {
        return reviewDAO.findByUserAndProduct(userId, productId).isPresent();
    }

    /** Whether the buyer may review the product right now (purchased and not yet reviewed). */
    public boolean canReview(long userId, long productId) throws SQLException {
        return orderDAO.hasPurchased(userId, productId) && !hasReviewed(userId, productId);
    }
}