package com.ayeshamart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(HomeServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("AyeshaMart home page requested");

        request.setAttribute("appName", "AyeshaMart");
        request.setAttribute("tagline", "Multi-Seller E-Commerce");

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}