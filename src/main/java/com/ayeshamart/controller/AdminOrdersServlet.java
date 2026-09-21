package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Order;
import com.ayeshamart.service.AdminService;
import com.ayeshamart.service.OrderStatus;

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
 * Admin order management (Phase 7). ADMIN-only via AuthFilter.
 * - GET  /admin/orders                    -> list/search all orders.
 * - GET  /admin/orders/details?id=        -> one order with full details.
 * - POST /admin/orders/status             -> advance order status.
 */
@WebServlet(urlPatterns = {"/admin/orders", "/admin/orders/details", "/admin/orders/status"})
public class AdminOrdersServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getRequestURI().endsWith("/details")) {
            showDetails(request, response);
        } else {
            showList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getRequestURI().endsWith("/status")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long orderId = parseId(request.getParameter("orderId"));
        String target = request.getParameter("status");
        if (orderId <= 0 || target == null || target.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/admin/orders?error="
                    + urlEncode("Invalid status update"));
            return;
        }
        try {
            adminService.updateStatus(orderId, target);
            response.sendRedirect(request.getContextPath() + "/admin/orders/details?id="
                    + orderId + "&message=" + urlEncode("Order status updated to " + target.trim().toUpperCase()));
        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath() + "/admin/orders/details?id="
                    + orderId + "&error=" + urlEncode(e.getMessage()));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/admin/orders/details?id="
                    + orderId + "&error=" + urlEncode("Could not update order status"));
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Order> orders = adminService.orders(request.getParameter("q"));
            request.setAttribute("orders", orders);
            request.setAttribute("q", request.getParameter("q"));
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("error", request.getParameter("error"));
            request.setAttribute("appName", "AyeshaMart Admin");
            request.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load orders");
            request.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(request, response);
        }
    }

    private void showDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long orderId = parseId(request.getParameter("id"));
        if (orderId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }
        try {
            Order order = adminService.orderForAdmin(orderId);
            request.setAttribute("order", order);
            request.setAttribute("allowedNext", OrderStatus.allowedNext(order.getStatus()));
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("error", request.getParameter("error"));
            request.setAttribute("appName", "AyeshaMart Admin");
            request.getRequestDispatcher("/WEB-INF/views/admin/order-details.jsp").forward(request, response);
        } catch (ValidationException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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