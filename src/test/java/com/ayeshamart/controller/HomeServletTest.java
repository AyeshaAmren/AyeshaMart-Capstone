package com.ayeshamart.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Test
    void homeRequestForwardsToHomeView() throws Exception {
        when(request.getRequestDispatcher("/WEB-INF/views/home.jsp")).thenReturn(dispatcher);

        new HomeServlet().doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/home.jsp");
        verify(dispatcher).forward(request, response);
    }
}