package com.ayeshamart.controller;

import com.ayeshamart.model.Product;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Seller dashboard: lists the logged-in seller's own products.
 * Protected by AuthFilter (requires SELLER role).
 */
@WebServlet("/seller/products")
public class SellerProductsServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long sellerId = AuthUtil.currentUserId(request);

        try {
            List<Product> products = productService.findBySeller(sellerId);
            request.setAttribute("products", products);
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("error", request.getParameter("error"));
            request.getRequestDispatcher("/WEB-INF/views/seller/my-products.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load products");
            request.getRequestDispatcher("/WEB-INF/views/seller/my-products.jsp").forward(request, response);
        }
    }
}