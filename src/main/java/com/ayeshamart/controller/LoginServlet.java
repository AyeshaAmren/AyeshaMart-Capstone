package com.ayeshamart.controller;

import com.ayeshamart.dto.LoginRequest;
import com.ayeshamart.exception.AuthenticationException;
import com.ayeshamart.model.User;
import com.ayeshamart.service.AuthService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles login form (GET) and authentication (POST),
 * then creates the session and sends the user to their role's home.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forwardToLogin(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        try {
            User user = authService.login(new LoginRequest(email, password));

            AuthUtil.login(request, user, redirect);

            String target = roleHome(user.getRole());
            String savedRedirect = AuthUtil.consumeRedirect(request);
            if (savedRedirect != null && savedRedirect.startsWith("/")) {
                target = savedRedirect;
            }

            response.sendRedirect(request.getContextPath() + target);
        } catch (AuthenticationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("email", email);
            forwardToLogin(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Login failed, please try again");
            forwardToLogin(request, response);
        }
    }

    private void forwardToLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    private String roleHome(String role) {
        if ("SELLER".equals(role)) {
            return "/seller/products";
        }
        return "/home";
    }
}