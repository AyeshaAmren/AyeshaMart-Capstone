package com.ayeshamart.controller;

import com.ayeshamart.model.AdminUser;
import com.ayeshamart.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Admin user management (Phase 7). ADMIN-only via AuthFilter. Search is
 * optional and always bound as PreparedStatement parameters.
 */
@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<AdminUser> users = adminService.users(request.getParameter("q"));
            request.setAttribute("users", users);
            request.setAttribute("q", request.getParameter("q"));
            request.setAttribute("appName", "AyeshaMart Admin");
            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load users");
            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
        }
    }
}