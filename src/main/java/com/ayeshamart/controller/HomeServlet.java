package com.ayeshamart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ayeshamart.model.Product;
import com.ayeshamart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(HomeServlet.class);

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("AyeshaMart home page requested");

        request.setAttribute("appName", "AyeshaMart");
        request.setAttribute("tagline", "Multi-Seller E-Commerce");

        request.setAttribute("featured", featuredProducts());
        request.setAttribute("categories", categories());

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }

    private List<Product> featuredProducts() {
        try {
            return productService.search(null, null);
        } catch (Exception e) {
            log.warn("Could not load featured products for home page", e);
            return new ArrayList<>();
        }
    }

    private List<String> categories() {
        try {
            return productService.categories();
        } catch (Exception e) {
            log.warn("Could not load categories for home page", e);
            return new ArrayList<>();
        }
    }
}