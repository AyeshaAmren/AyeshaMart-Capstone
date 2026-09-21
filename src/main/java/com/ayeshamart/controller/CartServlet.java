package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.service.CartService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Database-backed shopping cart (Phase 4).
 * - GET  /cart            -> shows the buyer's cart and total.
 * - POST /cart/add        -> add a product (increments existing row).
 * - POST /cart/update     -> set an absolute quantity (buttons / field).
 * - POST /cart/remove     -> drop a line.
 *
 * All paths are BUYER-only: AuthFilter rejects anonymous users (redirects
 * to login) and sellers/admins (403). The buyerId always comes from the
 * authenticated session, never from a request parameter.
 */
@WebServlet(urlPatterns = {"/cart", "/cart/add", "/cart/update", "/cart/remove"})
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long buyerId = AuthUtil.currentUserId(request);

        try {
            List<CartItem> items = cartService.cartFor(buyerId);
            BigDecimal total = cartService.cartTotal(buyerId);
            request.setAttribute("items", items);
            request.setAttribute("cartTotal", total);
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("error", request.getParameter("error"));
            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load your cart");
            request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        long buyerId = AuthUtil.currentUserId(request);
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.endsWith("/add")) {
            addProduct(buyerId, request, response);
        } else if (path.endsWith("/update")) {
            updateQuantity(buyerId, request, response);
        } else if (path.endsWith("/remove")) {
            removeProduct(buyerId, request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void addProduct(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long productId = parseId(request.getParameter("productId"));
        int quantity = parseInt(request.getParameter("quantity"), -1);

        if (productId <= 0) {
            redirect(request, response, "/products", "error", "Invalid product");
            return;
        }
        if (quantity <= 0) {
            redirect(request, response, "/products/detail?id=" + productId, "error", "Quantity must be greater than zero");
            return;
        }

        try {
            cartService.add(buyerId, productId, quantity);
            redirect(request, response, "/products/detail?id=" + productId, "added", "1");
        } catch (ValidationException e) {
            redirect(request, response, "/products/detail?id=" + productId, "error", e.getMessage());
        } catch (Exception e) {
            redirect(request, response, "/products/detail?id=" + productId, "error", "Could not add to cart");
        }
    }

    private void updateQuantity(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long productId = parseId(request.getParameter("productId"));
        int quantity = parseInt(request.getParameter("quantity"), -1);

        if (productId <= 0) {
            redirect(request, response, "/cart", "error", "Invalid product");
            return;
        }

        try {
            cartService.updateQuantity(buyerId, productId, quantity);
            redirect(request, response, "/cart", "message", "Cart updated");
        } catch (ValidationException e) {
            redirect(request, response, "/cart", "error", e.getMessage());
        } catch (Exception e) {
            redirect(request, response, "/cart", "error", "Could not update your cart");
        }
    }

    private void removeProduct(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long productId = parseId(request.getParameter("productId"));

        if (productId <= 0) {
            redirect(request, response, "/cart", "error", "Invalid product");
            return;
        }

        try {
            cartService.remove(buyerId, productId);
            redirect(request, response, "/cart", "message", "Item removed from cart");
        } catch (ValidationException e) {
            redirect(request, response, "/cart", "error", e.getMessage());
        } catch (Exception e) {
            redirect(request, response, "/cart", "error", "Could not remove the item");
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

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String path, String param, String value) throws IOException {
        String target = request.getContextPath() + path
                + (path.contains("?") ? "&" : "?")
                + param + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
        response.sendRedirect(target);
    }
}