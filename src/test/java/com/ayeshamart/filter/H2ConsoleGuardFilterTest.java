package com.ayeshamart.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class H2ConsoleGuardFilterTest {

    private final H2ConsoleGuardFilter filter = new H2ConsoleGuardFilter();

    @Mock
    private FilterChain chain;

    @AfterEach
    void clearSetting() {
        System.clearProperty(H2ConsoleGuardFilter.SETTING_PROPERTY);
    }

    private HttpServletRequest requestFrom(String remoteAddr, String remoteHost) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        lenient().when(request.getRemoteAddr()).thenReturn(remoteAddr);
        lenient().when(request.getRemoteHost()).thenReturn(remoteHost);
        return request;
    }

    private HttpServletResponse response() {
        return mock(HttpServletResponse.class);
    }

    @Test
    void allowsLoopbackByDefault() throws Exception {
        HttpServletResponse response = response();

        filter.doFilter(requestFrom("127.0.0.1", "127.0.0.1"), response, chain);

        verify(chain).doFilter(any(), any());
        verify(response, never()).sendError(any(Integer.class), any(String.class));
    }

    @Test
    void allowsLocalhostHostNameByDefault() throws Exception {
        HttpServletResponse response = response();

        filter.doFilter(requestFrom("::1", "localhost"), response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void blocksRemoteAddressesByDefault() throws Exception {
        HttpServletResponse response = response();

        filter.doFilter(requestFrom("203.0.113.42", "203.0.113.42"), response, chain);

        verify(response).sendError(any(Integer.class), any(String.class));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void allowsRemoteWhenExplicitlyOptedIn() throws Exception {
        System.setProperty(H2ConsoleGuardFilter.SETTING_PROPERTY, "true");
        HttpServletResponse response = response();

        filter.doFilter(requestFrom("203.0.113.42", "203.0.113.42"), response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void blocksEvenLoopbackWhenDisabledForProduction() throws Exception {
        System.setProperty(H2ConsoleGuardFilter.SETTING_PROPERTY, "false");
        HttpServletResponse response = response();

        filter.doFilter(requestFrom("127.0.0.1", "127.0.0.1"), response, chain);

        verify(response).sendError(any(Integer.class), any(String.class));
        verify(chain, never()).doFilter(any(), any());
    }
}