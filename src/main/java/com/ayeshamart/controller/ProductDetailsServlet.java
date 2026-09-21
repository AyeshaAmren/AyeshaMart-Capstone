package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.Review;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.service.ReviewService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Product details page: image, name, description, price, category, stock,
 * an "Add to Cart" form for logged-in users and the buyer reviews and
 * ratings (average + list + eligibility to review). Public read page.
 */
@WebServlet("/products/detail")
public class ProductDetailsServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long productId = parseId(request.getParameter("id"));
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product id");
            return;
        }

        try {
            Product product = productService.findDetail(productId);
            request.setAttribute("product", product);

            List<Review> reviews = reviewService.reviewsFor(productId);
            request.setAttribute("reviews", reviews);
            request.setAttribute("ratingCount", reviews.size());
            request.setAttribute("averageRating", averageOf(reviews));

            Long userId = AuthUtil.currentUserId(request);
            if (userId != null && "BUYER".equals(AuthUtil.currentRole(request))) {
                request.setAttribute("canReview", reviewService.canReview(userId, productId));
                request.setAttribute("hasReviewed", reviewService.hasReviewed(userId, productId));
            } else {
                request.setAttribute("canReview", false);
                request.setAttribute("hasReviewed", false);
            }

            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(request, response);
        } catch (ValidationException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private BigDecimal averageOf(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(reviews.stream()
                        .mapToInt(Review::getRating)
                        .average().orElse(0))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private long parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}