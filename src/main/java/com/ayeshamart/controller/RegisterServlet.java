package com.ayeshamart.controller;

import com.ayeshamart.dto.RegisterRequest;
import com.ayeshamart.exception.AuthenticationException;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Public registration for BUYER and SELLER roles.
 * ADMIN registration is rejected by AuthService.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role");

        RegisterRequest registerRequest = new RegisterRequest(name, email, password, confirmPassword, role);

        try {
            authService.register(registerRequest);
            response.sendRedirect(request.getContextPath() + "/login?registered=1");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.setAttribute("role", role);
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Registration failed, please try again");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}