package com.ayeshamart.controller;

import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.SellerOrderLine;
import com.ayeshamart.service.SellerService;
import com.ayeshamart.util.AuthUtil;

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
 * Seller incoming orders (Phase 6). SELLER-only via AuthFilter. All reads
 * are scoped by the seller's id at the database level and the status update
 * validates ownership plus the lifecycle transition on the server.
 * - GET  /seller/orders             -> incoming orders for this seller.
 * - GET  /seller/orders/details?id= -> this seller's lines of one order.
 * - POST /seller/orders/status      -> advance the status (server-validated).
 */
@WebServlet(urlPatterns = {"/seller/orders", "/seller/orders/details", "/seller/orders/status"})
public class SellerOrdersServlet extends HttpServlet {

    private final SellerService sellerService = new SellerService();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long sellerId = AuthUtil.currentUserId(request);
        if (request.getRequestURI().endsWith("/orders/details")) {
            showDetails(sellerId, request, response);
        } else {
            showList(sellerId, request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getRequestURI().endsWith("/orders/status")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long sellerId = AuthUtil.currentUserId(request);
        long orderId = parseId(request.getParameter("orderId"));
        String target = request.getParameter("status");
        if (orderId <= 0 || target == null || target.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/seller/orders?error="
                    + urlEncode("Invalid status update"));
            return;
        }

        try {
            sellerService.updateStatus(orderId, sellerId, target);
            response.sendRedirect(request.getContextPath() + "/seller/orders/details?id="
                    + orderId + "&message=" + urlEncode("Order status updated to " + target.trim().toUpperCase()));
        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath() + "/seller/orders/details?id="
                    + orderId + "&error=" + urlEncode(e.getMessage()));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/seller/orders/details?id="
                    + orderId + "&error=" + urlEncode("Could not update order status"));
        }
    }

    private void showList(long sellerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<SellerOrderLine> lines = sellerService.incomingOrders(sellerId);
            request.setAttribute("orders", lines);
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("error", request.getParameter("error"));
            request.setAttribute("appName", "AyeshaMart Seller");
            request.getRequestDispatcher("/WEB-INF/views/seller/seller-orders.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load your incoming orders");
            request.getRequestDispatcher("/WEB-INF/views/seller/seller-orders.jsp").forward(request, response);
        }
    }

    private void showDetails(long sellerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long orderId = parseId(request.getParameter("id"));
        if (orderId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order id");
            return;
        }
        try {
            List<SellerOrderLine> lines = sellerService.orderForSeller(orderId, sellerId);
            request.setAttribute("lines", lines);
            request.setAttribute("orderId", orderId);
            request.setAttribute("orderStatus", lines.get(0).getStatus());
            request.setAttribute("allowedNext", sellerService.allowedNextStatuses(lines.get(0).getStatus()));
            request.setAttribute("buyerName", lines.get(0).getBuyerName());
            request.setAttribute("orderDate", lines.get(0).getOrderDate());
            request.setAttribute("orderTotal", lines.stream()
                    .map(SellerOrderLine::getSubtotal).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

            Order order = orderDAO.findById(orderId);
            if (order != null) {
                request.setAttribute("shippingFullName", order.getShippingFullName());
                request.setAttribute("shippingPhone", order.getShippingPhone());
                request.setAttribute("shippingAddressLine1", order.getShippingAddressLine1());
                request.setAttribute("shippingAddressLine2", order.getShippingAddressLine2());
                request.setAttribute("shippingCity", order.getShippingCity());
                request.setAttribute("shippingState", order.getShippingState());
                request.setAttribute("shippingPincode", order.getShippingPincode());
                request.setAttribute("shippingLandmark", order.getShippingLandmark());
                request.setAttribute("paymentMethod", order.getPaymentMethod());
                request.setAttribute("paymentStatus", order.getPaymentStatus());
                request.setAttribute("paymentReference", order.getPaymentReference());
            }

            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("error", request.getParameter("error"));
            request.setAttribute("appName", "AyeshaMart Seller");
            request.getRequestDispatcher("/WEB-INF/views/seller/seller-order-details.jsp").forward(request, response);
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