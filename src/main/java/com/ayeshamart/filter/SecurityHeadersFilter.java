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
 * Applies safe default response headers to every application response (Phase 8).
 *
 * <ul>
 *   <li>{@code X-Content-Type-Options: nosniff} - browsers must not MIME-sniff,</li>
 *   <li>{@code X-Frame-Options: DENY} + CSP {@code frame-ancestors 'none'} -
 *       no page may be embedded in a cross-origin frame (clickjacking),</li>
 *   <li>{@code Referrer-Policy: no-referrer} - the browser sends no referrer,</li>
 *   <li>{@code Permissions-Policy} - camera/mic/geolocation are disabled,</li>
 *   <li>{@code Content-Security-Policy} - scripts only from this origin,
 *       forms/connections same-origin, images from data:/https: (used by the
 *       product art placeholders). Inline {@code style=""} is allowed because
 *       the UI uses it; inline JS is blocked.</li>
 * </ul>
 *
 * <p>The H2 console is deliberately skipped: it renders its own frames and
 * inline scripts, so a strict CSP would break the manual database management
 * that Phase 8 requires.
 */
@WebFilter(urlPatterns = {"/*"})
public class SecurityHeadersFilter implements Filter {

    private static final String CSP =
            "default-src 'self'; "
                    + "img-src 'self' data: https:; "
                    + "style-src 'self' 'unsafe-inline'; "
                    + "script-src 'self'; "
                    + "connect-src 'self'; "
                    + "object-src 'none'; "
                    + "frame-ancestors 'none'; "
                    + "base-uri 'self'; "
                    + "form-action 'self'";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (!path.startsWith("/h2-console")) {
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("Referrer-Policy", "no-referrer");
            httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
            httpResponse.setHeader("Content-Security-Policy", CSP);
        }

        chain.doFilter(request, response);
    }
}