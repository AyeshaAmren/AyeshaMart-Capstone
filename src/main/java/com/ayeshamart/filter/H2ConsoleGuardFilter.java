package com.ayeshamart.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Gives the H2 web console to local development only - never a public
 * production deployment.
 *
 * <p>Decision logic based on the {@code AYESHAMART_H2_CONSOLE} environment
 * variable (or {@code ayeshamart.h2.console} system property):
 * <ul>
 *   <li>{@code false} - always blocked (use in production).</li>
 *   <li>{@code true}  - allowed from anywhere (explicit opt-in, e.g. a TOTP
 *       protected staging box where that is deliberate).</li>
 *   <li>unset (default) - allowed only for loopback/localhost requests,
 *       which is exactly how local development works.</li>
 * </ul>
 */
@WebFilter(urlPatterns = {"/h2-console/*"})
public class H2ConsoleGuardFilter implements Filter {

    public static final String SETTING_PROPERTY = "ayeshamart.h2.console";
    public static final String SETTING_ENV = "AYESHAMART_H2_CONSOLE";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String setting = setting();
        if ("false".equalsIgnoreCase(setting)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "H2 console is disabled on this deployment.");
            return;
        }
        if ("true".equalsIgnoreCase(setting)) {
            chain.doFilter(request, response);
            return;
        }
        if (isLoopback(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                "H2 console is enabled for local development only.");
    }

    private String setting() {
        String property = System.getProperty(SETTING_PROPERTY);
        if (property != null && !property.isBlank()) {
            return property.trim();
        }
        return System.getenv(SETTING_ENV);
    }

    private boolean isLoopback(HttpServletRequest request) {
        String address = request.getRemoteAddr();
        if (address == null) {
            return false;
        }
        String host = request.getRemoteHost();
        return "127.0.0.1".equals(address)
                || "::1".equals(address)
                || "0:0:0:0:0:0:0:1".equals(address)
                || "localhost".equalsIgnoreCase(host);
    }
}