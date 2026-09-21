package com.ayeshamart.controller;

import com.ayeshamart.model.Product;
import com.ayeshamart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Buyer-facing product catalog with search (name/description) and an
 * exact category filter. Both work together. Public page.
 */
@WebServlet("/products")
public class ProductCatalogServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        String category = request.getParameter("category");

        try {
            List<Product> products = productService.search(query, category);
            request.setAttribute("products", products);
            request.setAttribute("q", query == null ? "" : query);
            request.setAttribute("selectedCategory",
                    category == null ? "" : category);
            request.setAttribute("categories", productService.categories());
            request.setAttribute("appName", "AyeshaMart");
            request.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Could not load the catalogue");
            request.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(request, response);
        }
    }
}