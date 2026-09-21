package com.ayeshamart.filter;

import com.ayeshamart.util.AuthUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Server-side role protection.
 * Only intercepts /seller/*, /buyer/* and /admin/*.
 * - not logged in -> redirect to the login page,
 * - wrong role     -> HTTP 403.
 * Public pages (home, login, register, css, js, h2-console) are never matched,
 * so they do not need a whitelist.
 */
@WebFilter(urlPatterns = {"/seller/*", "/buyer/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String requiredRole;
        if (path.startsWith("/seller")) {
            requiredRole = "SELLER";
        } else if (path.startsWith("/buyer")) {
            requiredRole = "BUYER";
        } else {
            requiredRole = "ADMIN";
        }

        HttpSession session = httpRequest.getSession(false);
        String role = session == null ? null : (String) session.getAttribute(AuthUtil.SESSION_USER_ROLE);

        if (role == null) {
            String redirect = URLEncoder.encode(path, StandardCharsets.UTF_8);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirect=" + redirect);
            return;
        }

        if (!requiredRole.equalsIgnoreCase(role)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied - this page requires the " + requiredRole + " role");
            return;
        }

        chain.doFilter(request, response);
    }
}