package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Product;
import com.ayeshamart.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Admin product moderation (Phase 7). ADMIN-only via AuthFilter.
 * - GET  /admin/products          -> list/search all listings.
 * - POST /admin/products/remove   -> remove a listing (or unlist it).
 */
@WebServlet({"/admin/products", "/admin/products/remove"})
public class AdminProductsServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getRequestURI().endsWith("/remove")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            List<Product> products = adminService.products(request.getParameter("q"));
            request.setAttribute("products", products);
            request.setAttribute("q", request.getParameter("q"));
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("error", request.getParameter("error"));
            request.setAttribute("appName", "AyeshaMart Admin");
            request.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load products");
            request.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!request.getRequestURI().endsWith("/remove")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long productId = parseId(request.getParameter("id"));
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin/products?error="
                    + urlEncode("Invalid product id"));
            return;
        }
        try {
            String result = adminService.removeListing(productId);
            response.sendRedirect(request.getContextPath() + "/admin/products?message="
                    + urlEncode("Listing removed "
                    + ("removed".equals(result) ? "from the store" : "(was referenced by orders, so it was unlisted instead)")));
        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath() + "/admin/products?error="
                    + urlEncode(e.getMessage()));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/admin/products?error="
                    + urlEncode("Could not remove the listing"));
        }
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

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}