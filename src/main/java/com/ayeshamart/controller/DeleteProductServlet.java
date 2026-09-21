package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.service.ProductService;
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
 * Deletes one of the seller's own products (POST only).
 */
@WebServlet("/seller/products/delete")
public class DeleteProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long sellerId = AuthUtil.currentUserId(request);
        long productId = parseId(request);

        String base = request.getContextPath() + "/seller/products";
        if (productId <= 0) {
            response.sendRedirect(base);
            return;
        }

        try {
            productService.delete(sellerId, productId);
            response.sendRedirect(base + "?message="
                    + URLEncoder.encode("Product deleted successfully", StandardCharsets.UTF_8));
        } catch (ValidationException e) {
            response.sendRedirect(base + "?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            response.sendRedirect(base + "?error="
                    + URLEncoder.encode("Could not delete product", StandardCharsets.UTF_8));
        }
    }

    private long parseId(HttpServletRequest request) {
        try {
            return Long.parseLong(request.getParameter("id"));
        } catch (Exception e) {
            return -1;
        }
    }
}