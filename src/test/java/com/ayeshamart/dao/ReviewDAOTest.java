package com.ayeshamart.dao;

import com.ayeshamart.model.Product;
import com.ayeshamart.model.Review;
import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ReviewDAO tests (Phase 5) against the real production schema (which
 * enforces a (user_id, product_id) unique constraint on reviews).
 */
class ReviewDAOTest {

    private ReviewDAO reviewDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private long buyer;
    private long productId;

    @BeforeEach
    void setUp() throws Exception {
        ConnectionManager.setDataSource(TestDb.create("reviewdaotest-" + System.nanoTime()));
        reviewDAO = new ReviewDAO();
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        buyer = userDAO.create(new User("Reviewer", "rev+rdt@example.com", "hash", "BUYER")).getId();

        Product product = new Product();
        product.setSellerId(userDAO.create(
                new User("Seller", "rs+rdt@example.com", "hash", "SELLER")).getId());
        product.setName("Reviewed Product");
        product.setDescription("desc");
        product.setPrice(new BigDecimal("20.00"));
        product.setStockQty(5);
        product.setCategory("Books");
        productId = productDAO.create(product).getId();
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.close();
    }

    private Review review(int rating, String comment) throws Exception {
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(buyer);
        review.setRating(rating);
        review.setComment(comment);
        return reviewDAO.insert(review);
    }

    @Test
    void insertSetsIdAndCreatedAt() throws Exception {
        Review saved = review(4, "Loved it");

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findByProductIdReturnsReviewsNewestFirstWithUserName() throws Exception {
        review(3, "ok");

        List<Review> reviews = reviewDAO.findByProductId(productId);

        assertEquals(1, reviews.size());
        assertEquals("Reviewer", reviews.get(0).getUserName());
        assertEquals(3, reviews.get(0).getRating());
        assertEquals("ok", reviews.get(0).getComment());
    }

    @Test
    void findByUserAndProductFindsExistingReview() throws Exception {
        review(5, "Excellent");

        Optional<Review> found = reviewDAO.findByUserAndProduct(buyer, productId);
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getRating());
    }

    @Test
    void findByUserAndProductIsEmptyWhenNoReview() throws Exception {
        Optional<Review> found = reviewDAO.findByUserAndProduct(buyer, productId);
        assertFalse(found.isPresent());
    }

    @Test
    void duplicateReviewViolatesUniqueConstraint() throws Exception {
        review(4, "first");

        SQLException error = assertThrows(SQLException.class, () -> review(5, "second"));
        assertTrue(error.getSQLState() != null && error.getSQLState().startsWith("23"));
    }
}