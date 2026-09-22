package com.ayeshamart.controller;

import com.ayeshamart.service.HealthService;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Health check endpoint (Phase 8).
 *
 * <pre>
 *   GET /api/v1/health
 *   {"service":"ayeshamart","status":"UP","database":"UP","timestamp":"..."}
 * </pre>
 *
 * <p>Public and always returns 200 with the structure above so a load balancer
 * or monitoring probe can read {@code status}/{@code database} directly.
 */
@WebServlet("/api/v1/health")
public class HealthServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    private final HealthService healthService = new HealthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(healthService.check()));
    }
}