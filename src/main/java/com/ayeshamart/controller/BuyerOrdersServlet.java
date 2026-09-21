package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Order;
import com.ayeshamart.service.OrderService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Buyer order pages (Phase 5). All paths are BUYER-only via AuthFilter and
 * every order lookup is scoped to the authenticated buyer, so changing the
 * id in the URL can never reveal another buyer's order.
 * - GET /buyer/orders               -> my orders list.
 * - GET /buyer/orders/details       -> one order with its line items.
 * - GET /buyer/orders/confirmation  -> post-payment order confirmation.
 */
@WebServlet(urlPatterns = {"/buyer/orders", "/buyer/orders/details", "/buyer/orders/confirmation"})
public class BuyerOrdersServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long buyerId = AuthUtil.currentUserId(request);
        String uri = request.getRequestURI();

        if (uri.endsWith("/orders/details")) {
            showDetails(buyerId, request, response);
        } else if (uri.endsWith("/orders/confirmation")) {
            showConfirmation(buyerId, request, response);
        } else {
            showList(buyerId, request, response);
        }
    }

    private void showList(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Order> orders = orderService.ordersFor(buyerId);
            request.setAttribute("orders", orders);
            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/buyer/my-orders.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load your orders");
            request.getRequestDispatcher("/WEB-INF/views/buyer/my-orders.jsp").forward(request, response);
        }
    }

    private void showDetails(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long orderId = parseId(request.getParameter("id"));
        if (orderId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }
        try {
            Order order = orderService.orderForBuyer(buyerId, orderId);
            request.setAttribute("order", order);
            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/buyer/order-details.jsp").forward(request, response);
        } catch (ValidationException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void showConfirmation(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long orderId = parseId(request.getParameter("id"));
        if (orderId <= 0) {
            response.sendRedirect(request.getContextPath() + "/buyer/orders");
            return;
        }
        try {
            Order order = orderService.orderForBuyer(buyerId, orderId);
            request.setAttribute("order", order);
            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/buyer/order-confirmation.jsp").forward(request, response);
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
}