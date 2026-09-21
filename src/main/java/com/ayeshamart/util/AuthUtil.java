package com.ayeshamart.util;

import com.ayeshamart.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Small helpers for the authenticated session so controllers and
 * views do not repeat the same session logic.
 */
public final class AuthUtil {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_ROLE = "userRole";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_REDIRECT = "redirect";

    private AuthUtil() {
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return currentUserId(request) != null;
    }

    public static Long currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Long) session.getAttribute(SESSION_USER_ID);
    }

    public static String currentRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(SESSION_USER_ROLE);
    }

    public static boolean hasRole(HttpServletRequest request, String role) {
        return role != null && role.equals(currentRole(request));
    }

    /**
     * Starts the authenticated session: sets a timeout, regenerates the
     * session id (session fixation protection), then stores user data.
     */
    public static void login(HttpServletRequest request, User user, String redirect) {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USER_ROLE, user.getRole());
        session.setAttribute(SESSION_USER_NAME, user.getName());
        if (redirect != null && !redirect.isBlank()) {
            session.setAttribute(SESSION_REDIRECT, redirect);
        }
    }

    public static String consumeRedirect(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        String redirect = (String) session.getAttribute(SESSION_REDIRECT);
        session.removeAttribute(SESSION_REDIRECT);
        return redirect;
    }
}