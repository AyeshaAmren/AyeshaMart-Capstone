package com.ayeshamart.controller;

import com.ayeshamart.model.SellerStats;
import com.ayeshamart.service.SellerService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Seller Dashboard (Phase 6). SELLER-only via AuthFilter. Every figure is
 * computed from the database for the logged-in seller's own products and
 * incoming orders - statistics are never hardcoded.
 */
@WebServlet("/seller/dashboard")
public class SellerDashboardServlet extends HttpServlet {

    private final SellerService sellerService = new SellerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            SellerStats stats = sellerService.stats(AuthUtil.currentUserId(request));
            request.setAttribute("stats", stats);
            request.setAttribute("appName", "AyeshaMart Seller");
            request.getRequestDispatcher("/WEB-INF/views/seller/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load your dashboard");
            request.getRequestDispatcher("/WEB-INF/views/seller/dashboard.jsp").forward(request, response);
        }
    }
}