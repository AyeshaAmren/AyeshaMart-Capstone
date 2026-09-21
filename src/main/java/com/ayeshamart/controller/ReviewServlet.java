package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.service.ReviewService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Submits a buyer review for a purchased product (Phase 5).
 * BUYER-only via AuthFilter. All validation (rating range, comment length,
 * purchase eligibility, one review per product) lives in ReviewService.
 */
@WebServlet("/buyer/review")
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        long buyerId = AuthUtil.currentUserId(request);
        long productId = parseId(request.getParameter("productId"));
        int rating = parseInt(request.getParameter("rating"), -1);
        String comment = request.getParameter("comment");

        try {
            reviewService.submit(buyerId, productId, rating, comment);
            response.sendRedirect(request.getContextPath() + "/products/detail?id=" + productId
                    + "&reviewed=1");
        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath() + "/products/detail?id=" + productId
                    + "&error=" + urlEncode(e.getMessage()));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products/detail?id=" + productId
                    + "&error=" + urlEncode("Could not submit your review. Please try again."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/products");
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

    private int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}