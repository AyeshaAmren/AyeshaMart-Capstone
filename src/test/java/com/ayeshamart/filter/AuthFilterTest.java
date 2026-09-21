package com.ayeshamart.filter;

import com.ayeshamart.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private AuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthFilter();
    }

    private HttpSession sessionWithRole(String role) {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(AuthUtil.SESSION_USER_ROLE)).thenReturn(role);
        return session;
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/seller/products");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/ayeshamart/login?redirect=%2Fseller%2Fproducts");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void sellerPassesOnSellerPage() throws Exception {
        HttpSession session = sessionWithRole("SELLER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/seller/products");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void buyerGetsForbiddenOnSellerPage() throws Exception {
        HttpSession session = sessionWithRole("BUYER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/seller/products");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(403, "Access denied - this page requires the SELLER role");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void nonAdminGetsForbiddenOnAdminPage() throws Exception {
        HttpSession session = sessionWithRole("SELLER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/admin/users");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(403, "Access denied - this page requires the ADMIN role");
    }

    @Test
    void adminPassesOnAdminPage() throws Exception {
        HttpSession session = sessionWithRole("ADMIN");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/admin/users");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void anonymousUserIsRedirectedToLoginOnCart() throws Exception {
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/cart");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/ayeshamart/login?redirect=%2Fcart");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void buyerPassesOnCart() throws Exception {
        HttpSession session = sessionWithRole("BUYER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/cart");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void sellerGetsForbiddenOnCart() throws Exception {
        HttpSession session = sessionWithRole("SELLER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/cart");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(403, "Access denied - this page requires the BUYER role");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void anonymousUserIsRedirectedToLoginOnBuyerCheckout() throws Exception {
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/buyer/checkout");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/ayeshamart/login?redirect=%2Fbuyer%2Fcheckout");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void buyerPassesOnBuyerOrders() throws Exception {
        HttpSession session = sessionWithRole("BUYER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/buyer/orders");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void sellerGetsForbiddenOnBuyerPages() throws Exception {
        HttpSession session = sessionWithRole("SELLER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/buyer/review");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(403, "Access denied - this page requires the BUYER role");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void anonymousUserIsRedirectedToLoginOnSellerOrders() throws Exception {
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/seller/orders");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/ayeshamart/login?redirect=%2Fseller%2Forders");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void buyerGetsForbiddenOnSellerDashboard() throws Exception {
        HttpSession session = sessionWithRole("BUYER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/seller/dashboard");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(403, "Access denied - this page requires the SELLER role");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void adminGetsForbiddenOnSellerOrders() throws Exception {
        HttpSession session = sessionWithRole("ADMIN");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/seller/orders");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(403, "Access denied - this page requires the SELLER role");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void sellerPassesOnSellerOrders() throws Exception {
        HttpSession session = sessionWithRole("SELLER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/seller/orders");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void anonymousUserIsRedirectedToLoginOnAdminOrders() throws Exception {
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/admin/orders");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/ayeshamart/login?redirect=%2Fadmin%2Forders");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void adminPassesOnAdminDashboard() throws Exception {
        HttpSession session = sessionWithRole("ADMIN");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/admin/dashboard");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void buyerGetsForbiddenOnAdminProducts() throws Exception {
        HttpSession session = sessionWithRole("BUYER");
        when(request.getContextPath()).thenReturn("/ayeshamart");
        when(request.getRequestURI()).thenReturn("/ayeshamart/admin/products");
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, chain);

        verify(response).sendError(403, "Access denied - this page requires the ADMIN role");
        verify(chain, never()).doFilter(any(), any());
    }
}