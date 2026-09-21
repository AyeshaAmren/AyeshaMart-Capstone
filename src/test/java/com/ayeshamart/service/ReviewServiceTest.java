package com.ayeshamart.service;

import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.ReviewDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ReviewService tests (Phase 5): rating bounds, comment length, purchase
 * eligibility, duplicate prevention and delegation.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;
    @Mock
    private OrderDAO orderDAO;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewDAO, orderDAO);
    }

    @Test
    void submitInsertsValidReviewForPurchasedProduct() throws Exception {
        when(orderDAO.hasPurchased(1L, 10L)).thenReturn(true);
        when(reviewDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.empty());

        Review result = new Review();
        when(reviewDAO.insert(any(Review.class))).thenReturn(result);

        Review saved = reviewService.submit(1L, 10L, 4, "  Great book!  ");

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewDAO).insert(captor.capture());
        assertEquals(10L, captor.getValue().getProductId());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(4, captor.getValue().getRating());
        assertEquals("Great book!", captor.getValue().getComment());
        assertEquals(saved, result);
    }

    @Test
    void submitStoresNullWhenCommentIsBlank() throws Exception {
        when(orderDAO.hasPurchased(1L, 10L)).thenReturn(true);
        when(reviewDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.empty());

        reviewService.submit(1L, 10L, 5, "   ");

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewDAO).insert(captor.capture());
        assertNull(captor.getValue().getComment());
    }

    @Test
    void submitRejectsRatingOutsideOneToFive() throws Exception {
        assertThrows(ValidationException.class, () -> reviewService.submit(1L, 10L, 0, "ok"));
        assertThrows(ValidationException.class, () -> reviewService.submit(1L, 10L, 6, "ok"));
        assertThrows(ValidationException.class, () -> reviewService.submit(1L, 10L, -1, "ok"));
        verify(reviewDAO, never()).insert(any(Review.class));
    }

    @Test
    void submitRejectsOverlongComment() throws Exception {
        String tooLong = "x".repeat(ReviewService.MAX_COMMENT_LENGTH + 1);

        ValidationException error =
                assertThrows(ValidationException.class, () -> reviewService.submit(1L, 10L, 4, tooLong));
        assertTrue(error.getMessage().contains("Comment"));
        verify(reviewDAO, never()).insert(any(Review.class));
    }

    @Test
    void submitRejectsProductNotPurchased() throws Exception {
        when(orderDAO.hasPurchased(1L, 10L)).thenReturn(false);

        ValidationException error =
                assertThrows(ValidationException.class, () -> reviewService.submit(1L, 10L, 4, "ok"));
        assertTrue(error.getMessage().contains("purchased"));
        verify(reviewDAO, never()).insert(any(Review.class));
    }

    @Test
    void submitRejectsDuplicateReview() throws Exception {
        when(orderDAO.hasPurchased(1L, 10L)).thenReturn(true);
        when(reviewDAO.findByUserAndProduct(1L, 10L))
                .thenReturn(Optional.of(new Review()));

        ValidationException error =
                assertThrows(ValidationException.class, () -> reviewService.submit(1L, 10L, 4, "ok"));
        assertTrue(error.getMessage().contains("already reviewed"));
        verify(reviewDAO, never()).insert(any(Review.class));
    }

    @Test
    void submitVetoesInvalidProductId() throws Exception {
        assertThrows(ValidationException.class, () -> reviewService.submit(1L, 0L, 4, "ok"));
        verify(reviewDAO, never()).insert(any(Review.class));
    }

    @Test
    void reviewsForReturnsProductReviews() throws Exception {
        when(reviewDAO.findByProductId(10L)).thenReturn(List.of(new Review()));

        assertEquals(1, reviewService.reviewsFor(10L).size());
        verify(reviewDAO).findByProductId(10L);
    }

    @Test
    void canReviewRequiresPurchaseAndNoExistingReview() throws Exception {
        when(orderDAO.hasPurchased(1L, 10L)).thenReturn(true);
        when(reviewDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.empty());
        assertTrue(reviewService.canReview(1L, 10L));

        when(reviewDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.of(new Review()));
        assertFalse(reviewService.canReview(1L, 10L));
    }
}