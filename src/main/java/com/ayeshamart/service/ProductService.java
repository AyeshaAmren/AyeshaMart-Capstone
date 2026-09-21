package com.ayeshamart.service;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dto.ProductForm;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Business logic for seller product management.
 * Validates all input server side and enforces ownership: a seller can
 * only create, edit or delete their own products.
 */
public class ProductService {

    /** Default catalogue categories shown when the products table is empty. */
    public static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Electronics", "Fashion", "Grocery", "Beauty",
            "Home/Kitchen", "Books", "Sports", "Accessories");

    private final ProductDAO productDAO;

    public ProductService() {
        this(new ProductDAO());
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Buyer catalog lookup. Search is by name and/or description and may be
     * combined with an exact category filter. Only in-stock products are
     * returned.
     */
    public List<Product> search(String query, String category) throws SQLException {
        return productDAO.search(query, category);
    }

    /** Reads one product for the product details page; throws if missing. */
    public Product findDetail(long productId) throws SQLException {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new ValidationException("Product not found");
        }
        return product;
    }

    /**
     * Category options for the filter drop-down: all categories currently
     * present in the database, falling back to the standard list when there
     * is no data yet.
     */
    public List<String> categories() throws SQLException {
        List<String> fromDb = productDAO.findCategories();
        return fromDb.isEmpty() ? new ArrayList<>(DEFAULT_CATEGORIES) : fromDb;
    }

    public Product create(long sellerId, ProductForm form) throws SQLException {
        Product product = new Product();
        applyForm(product, form);
        product.setSellerId(sellerId);
        return productDAO.create(product);
    }

    public List<Product> findBySeller(long sellerId) throws SQLException {
        return productDAO.findBySellerId(sellerId);
    }

    /**
     * Reads a product only if it belongs to the given seller.
     * Used by edit/delete to guarantee ownership checks in the service layer.
     */
    public Product findOwnedBySeller(long sellerId, long productId) throws SQLException {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new ValidationException("Product not found");
        }
        if (product.getSellerId() != sellerId) {
            throw new ValidationException("You can only manage your own products");
        }
        return product;
    }

    public void update(long sellerId, long productId, ProductForm form) throws SQLException {
        findOwnedBySeller(sellerId, productId);

        Product product = new Product();
        applyForm(product, form);

        if (!productDAO.update(productId, sellerId, product)) {
            throw new ValidationException("Product not found");
        }
    }

    public void delete(long sellerId, long productId) throws SQLException {
        findOwnedBySeller(sellerId, productId);

        if (!productDAO.delete(productId, sellerId)) {
            throw new ValidationException("Product not found");
        }
    }

    private void applyForm(Product product, ProductForm form) {
        String name = form.getName();
        String category = form.getCategory();
        String imageUrl = form.getImageUrl();

        if (name == null || name.isBlank()) {
            throw new ValidationException("Product name is required");
        }
        if (category == null || category.isBlank()) {
            throw new ValidationException("Category is required");
        }
        if (imageUrl != null && !imageUrl.isBlank()
                && !imageUrl.startsWith("http://")
                && !imageUrl.startsWith("https://")
                && !imageUrl.startsWith("/")) {
            throw new ValidationException("Image URL must be a valid http(s) or /path URL");
        }

        BigDecimal price;
        try {
            price = new BigDecimal(form.getPrice().trim());
        } catch (Exception e) {
            throw new ValidationException("Price must be a valid number");
        }
        if (price.signum() < 0) {
            throw new ValidationException("Price cannot be negative");
        }

        int stockQty;
        try {
            stockQty = Integer.parseInt(form.getStockQty().trim());
        } catch (Exception e) {
            throw new ValidationException("Stock quantity must be a valid integer");
        }
        if (stockQty < 0) {
            throw new ValidationException("Stock quantity cannot be negative");
        }

        product.setName(name.trim());
        product.setDescription(form.getDescription() == null ? null : form.getDescription().trim());
        product.setPrice(price);
        product.setStockQty(stockQty);
        product.setCategory(category.trim());
        product.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
    }
}