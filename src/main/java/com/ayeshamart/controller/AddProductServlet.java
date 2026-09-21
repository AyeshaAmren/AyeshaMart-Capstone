package com.ayeshamart.controller;

import com.ayeshamart.dto.ProductForm;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Displays the "Add product" form (GET) and saves a new product (POST).
 */
@WebServlet("/seller/products/add")
public class AddProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mode", "add");
        request.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long sellerId = AuthUtil.currentUserId(request);
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String price = request.getParameter("price");
        String stockQty = request.getParameter("stockQty");
        String category = request.getParameter("category");
        String imageUrl = request.getParameter("imageUrl");

        try {
            productService.create(sellerId,
                    new ProductForm(name, description, price, stockQty, category, imageUrl));
            response.sendRedirect(request.getContextPath()
                    + "/seller/products?message=" + encode("Product added successfully"));
        } catch (ValidationException e) {
            reShowForm(request, response, e.getMessage(), name, description, price, stockQty, category, imageUrl);
        } catch (Exception e) {
            reShowForm(request, response, "Could not add product, please try again",
                    name, description, price, stockQty, category, imageUrl);
        }
    }

    private void reShowForm(HttpServletRequest request, HttpServletResponse response,
                            String error, String name, String description, String price,
                            String stockQty, String category, String imageUrl)
            throws ServletException, IOException {
        request.setAttribute("mode", "add");
        request.setAttribute("error", error);
        request.setAttribute("name", name);
        request.setAttribute("description", description);
        request.setAttribute("price", price);
        request.setAttribute("stockQty", stockQty);
        request.setAttribute("category", category);
        request.setAttribute("imageUrl", imageUrl);
        request.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(request, response);
    }

    private String encode(String value) throws IOException {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}