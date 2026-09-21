package com.ayeshamart.controller;

import com.ayeshamart.dto.ProductForm;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Product;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Displays the prefilled "Edit product" form (GET) and saves changes (POST).
 * Ownership is enforced in the service layer.
 */
@WebServlet("/seller/products/edit")
public class EditProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long sellerId = AuthUtil.currentUserId(request);
        long productId = parseId(request);
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/seller/products");
            return;
        }

        try {
            Product product = productService.findOwnedBySeller(sellerId, productId);
            request.setAttribute("mode", "edit");
            request.setAttribute("product", product);
            request.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath()
                    + "/seller/products?error=" + URLEncoder.encode("Could not load product", StandardCharsets.UTF_8));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long sellerId = AuthUtil.currentUserId(request);
        long productId = parseId(request);
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/seller/products");
            return;
        }

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String price = request.getParameter("price");
        String stockQty = request.getParameter("stockQty");
        String category = request.getParameter("category");
        String imageUrl = request.getParameter("imageUrl");

        try {
            productService.update(sellerId, productId,
                    new ProductForm(name, description, price, stockQty, category, imageUrl));
            response.sendRedirect(request.getContextPath()
                    + "/seller/products?message=" + URLEncoder.encode("Product updated successfully", StandardCharsets.UTF_8));
        } catch (ValidationException e) {
            request.setAttribute("mode", "edit");
            request.setAttribute("productId", productId);
            request.setAttribute("error", e.getMessage());
            request.setAttribute("name", name);
            request.setAttribute("description", description);
            request.setAttribute("price", price);
            request.setAttribute("stockQty", stockQty);
            request.setAttribute("category", category);
            request.setAttribute("imageUrl", imageUrl);
            request.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("mode", "edit");
            request.setAttribute("productId", productId);
            request.setAttribute("error", "Could not update product, please try again");
            request.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(request, response);
        }
    }

    private long parseId(HttpServletRequest request) {
        try {
            return Long.parseLong(request.getParameter("id"));
        } catch (Exception e) {
            return -1;
        }
    }
}