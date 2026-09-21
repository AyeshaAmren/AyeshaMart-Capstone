package com.ayeshamart.controller;

import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Order;
import com.ayeshamart.service.CartService;
import com.ayeshamart.service.OrderService;
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
 * Buyer checkout (Phase 5). All paths are BUYER-only via AuthFilter.
 * - GET  /buyer/checkout          -> order summary of the current cart.
 * - GET  /buyer/checkout/payment  -> mock payment form (server recalculates total).
 * - POST /buyer/checkout/pay      -> validate mock payment, place the order.
 *
 * The buyer id always comes from the session and every price, quantity and
 * total is recomputed from the database by OrderService - nothing from the
 * browser is trusted for the amounts.
 */
@WebServlet(urlPatterns = {"/buyer/checkout", "/buyer/checkout/payment", "/buyer/checkout/pay"})
public class BuyerCheckoutServlet extends HttpServlet {

    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long buyerId = AuthUtil.currentUserId(request);
        if (request.getRequestURI().endsWith("/payment")) {
            showPayment(buyerId, request, response);
        } else {
            showCheckout(buyerId, request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long buyerId = AuthUtil.currentUserId(request);
        if (request.getRequestURI().endsWith("/pay")) {
            processPayment(buyerId, request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showCheckout(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<CartItem> items = cartService.cartFor(buyerId);
            if (items.isEmpty()) {
                request.getSession(true).setAttribute("checkoutNotice", "Your cart is empty - add products before checking out");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            request.setAttribute("items", items);
            request.setAttribute("cartTotal", cartService.cartTotal(buyerId));
            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/buyer/checkout.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not open checkout");
            request.getRequestDispatcher("/WEB-INF/views/buyer/checkout.jsp").forward(request, response);
        }
    }

    private void showPayment(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<CartItem> items = cartService.cartFor(buyerId);
            if (items.isEmpty()) {
                request.getSession(true).setAttribute("checkoutNotice", "Your cart is empty - add products before checking out");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            request.setAttribute("items", items);
            request.setAttribute("cartTotal", cartService.cartTotal(buyerId));
            request.setAttribute("error", request.getParameter("error"));
            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/buyer/payment.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/buyer/checkout?error="
                    + urlEncode("Could not open payment"));
        }
    }

    private void processPayment(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String paymentError = validatePaymentForm(request);
        if (paymentError != null) {
            response.sendRedirect(request.getContextPath() + "/buyer/checkout/payment?error="
                    + urlEncode(paymentError));
            return;
        }

        try {
            Order order = orderService.placeOrder(buyerId);
            response.sendRedirect(request.getContextPath()
                    + "/buyer/orders/confirmation?id=" + order.getId());
        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath() + "/buyer/checkout/payment?error="
                    + urlEncode(e.getMessage()));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/buyer/checkout/payment?error="
                    + urlEncode("Payment could not be completed. Please try again."));
        }
    }

    private String validatePaymentForm(HttpServletRequest request) {
        String name = request.getParameter("cardName");
        String number = request.getParameter("cardNumber");
        String expiry = request.getParameter("expiry");
        String cvv = request.getParameter("cvv");

        if (name == null || name.isBlank()) {
            return "Cardholder name is required";
        }
        if (number == null || !number.replaceAll("\\s", "").matches("\\d{12,19}")) {
            return "Enter a valid card number (12-19 digits)";
        }
        if (expiry == null || !expiry.replaceAll("\\s", "").matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return "Enter a valid expiry date (MM/YY)";
        }
        if (cvv == null || !cvv.trim().matches("\\d{3,4}")) {
            return "Enter a valid CVV (3-4 digits)";
        }
        return null;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}