package com.ayeshamart.controller;

import com.ayeshamart.dto.PaymentDetails;
import com.ayeshamart.dto.ShippingDetails;
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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Buyer checkout (Phase 5 + 7). All paths are BUYER-only via AuthFilter.
 * - GET  /buyer/checkout          -> order summary + shipping details form.
 * - POST /buyer/checkout          -> validate + save the shipping snapshot.
 * - GET  /buyer/checkout/payment  -> mock payment method selection.
 * - POST /buyer/checkout/pay      -> validate mock payment, place the order.
 *
 * The buyer id always comes from the session and every price, quantity and
 * total is recomputed from the database by OrderService - nothing from the
 * browser is trusted for the amounts. Card/UPI values are validated on the
 * server and never stored; only method, status and reference are saved.
 */
@WebServlet(urlPatterns = {"/buyer/checkout", "/buyer/checkout/payment", "/buyer/checkout/pay"})
public class BuyerCheckoutServlet extends HttpServlet {

    static final String SESSION_SHIPPING = "pendingShipping";

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
        } else if (request.getRequestURI().endsWith("/checkout")) {
            saveShipping(buyerId, request, response);
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

    private void saveShipping(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ShippingDetails shipping = new ShippingDetails(
                request.getParameter("fullName"),
                request.getParameter("phone"),
                request.getParameter("addressLine1"),
                request.getParameter("addressLine2"),
                request.getParameter("city"),
                request.getParameter("state"),
                request.getParameter("pincode"),
                request.getParameter("landmark"));

        if (!shipping.isValid()) {
            request.setAttribute("shipping", shipping);
            request.setAttribute("error", shipping.validate().get(0));
            showCheckout(buyerId, request, response);
            return;
        }

        request.getSession(true).setAttribute(SESSION_SHIPPING, shipping);
        response.sendRedirect(request.getContextPath() + "/buyer/checkout/payment");
    }

    private void showPayment(long buyerId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        ShippingDetails shipping = session == null ? null
                : (ShippingDetails) session.getAttribute(SESSION_SHIPPING);
        if (shipping == null) {
            response.sendRedirect(request.getContextPath() + "/buyer/checkout");
            return;
        }
        try {
            List<CartItem> items = cartService.cartFor(buyerId);
            if (items.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/buyer/checkout");
                return;
            }
            request.setAttribute("items", items);
            request.setAttribute("cartTotal", cartService.cartTotal(buyerId));
            request.setAttribute("shipping", shipping);
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
        HttpSession session = request.getSession(false);
        ShippingDetails shipping = session == null ? null
                : (ShippingDetails) session.getAttribute(SESSION_SHIPPING);
        if (shipping == null) {
            response.sendRedirect(request.getContextPath() + "/buyer/checkout");
            return;
        }

        PaymentDetails payment = new PaymentDetails(
                request.getParameter("paymentMethod"),
                request.getParameter("upiId"),
                request.getParameter("cardName"),
                request.getParameter("cardNumber"),
                request.getParameter("expiry"),
                request.getParameter("cvv"));

        try {
            Order order = orderService.placeOrder(buyerId, shipping, payment);
            if (session != null) {
                session.removeAttribute(SESSION_SHIPPING);
            }
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

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}