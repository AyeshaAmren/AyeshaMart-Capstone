package com.ayeshamart.controller;

import com.ayeshamart.model.AdminStats;
import com.ayeshamart.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin Dashboard (Phase 7). ADMIN-only via AuthFilter. Every figure is
 * computed from the database by AdminService - nothing is hardcoded.
 * Both /admin and /admin/dashboard render the dashboard so the header
 * "Admin" link works.
 */
@WebServlet({"/admin", "/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            AdminStats stats = adminService.stats();
            request.setAttribute("stats", stats);
            request.setAttribute("appName", "AyeshaMart Admin");
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load the dashboard");
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
        }
    }
}