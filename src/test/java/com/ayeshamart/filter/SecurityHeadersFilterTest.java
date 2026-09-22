package com.ayeshamart.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersFilterTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

    @Mock
    private FilterChain chain;

    private HttpServletRequest requestFor(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getContextPath()).thenReturn("/ayeshamart");
        return request;
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void appliesSecurityHeadersToApplicationPages() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);

        filter.doFilter(requestFor("/ayeshamart/buyer/checkout"), response, chain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("Referrer-Policy", "no-referrer");
        verify(response).setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        verify(response).setHeader(ArgumentMatchers.eq("Content-Security-Policy"),
                contains("script-src 'self'"));
        verify(response).setHeader(ArgumentMatchers.eq("Content-Security-Policy"),
                contains("frame-ancestors 'none'"));
        verify(chain).doFilter(any(), any());
    }

    @Test
    void skipsHeadersForH2ConsoleSoManualDatabaseAccessKeepsWorking() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);

        filter.doFilter(requestFor("/ayeshamart/h2-console/login.do"), response, chain);

        verify(response, never()).setHeader(ArgumentMatchers.eq("Content-Security-Policy"), anyString());
        verify(response, never()).setHeader(ArgumentMatchers.eq("X-Frame-Options"), anyString());
        verify(chain).doFilter(any(), any());
    }
}