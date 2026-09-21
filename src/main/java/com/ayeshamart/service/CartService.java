package com.ayeshamart.service;

import com.ayeshamart.dao.CartDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for the database-backed shopping cart (Phase 4).
 * Every method is scoped by an explicit buyerId supplied by the servlet
 * from the authenticated session, so one buyer can never read or modify
 * another buyer's cart.
 *
 * <p>Validation rules:
 * product must exist, quantity must be &gt; 0 and must not exceed the
 * available stock. Re-adding a product increments the existing cart row
 * instead of creating a duplicate.
 */
public class CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService() {
        this(new CartDAO(), new ProductDAO());
    }

    public CartService(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    /**
     * Adds {@code quantity} units of a product to the buyer's cart.
     * If the product is already in the cart the quantity is increased,
     * never duplicated.
     */
    public void add(long buyerId, long productId, int quantity) throws SQLException {
        Product product = requireProduct(productId);
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }

        Optional<CartItem> existing = cartDAO.findByUserAndProduct(buyerId, productId);
        int newQuantity = existing.map(CartItem::getQuantity).orElse(0) + quantity;

        if (newQuantity > product.getStockQty()) {
            throw new ValidationException(product.getStockQty() == 0
                    ? "This product is out of stock"
                    : "Quantity cannot exceed available stock (" + product.getStockQty() + ")");
        }

        if (existing.isPresent()) {
            cartDAO.updateQuantity(buyerId, productId, newQuantity);
        } else {
            cartDAO.insert(new CartItem(buyerId, productId, newQuantity));
        }
    }

    /**
     * Sets the absolute quantity of one cart line. Used by the +/- buttons
     * and the quantity field on the cart page.
     */
    public void updateQuantity(long buyerId, long productId, int quantity) throws SQLException {
        Product product = requireProduct(productId);
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }
        if (quantity > product.getStockQty()) {
            throw new ValidationException(product.getStockQty() == 0
                    ? "This product is out of stock"
                    : "Quantity cannot exceed available stock (" + product.getStockQty() + ")");
        }
        if (!cartDAO.updateQuantity(buyerId, productId, quantity)) {
            throw new ValidationException("This item is not in your cart");
        }
    }

    public void remove(long buyerId, long productId) throws SQLException {
        if (!cartDAO.delete(buyerId, productId)) {
            throw new ValidationException("This item is not in your cart");
        }
    }

    public List<CartItem> cartFor(long buyerId) throws SQLException {
        return cartDAO.findByUserId(buyerId);
    }

    /** Sum of unit price * quantity across the buyer's cart lines. */
    public BigDecimal cartTotal(long buyerId) throws SQLException {
        return cartDAO.findByUserId(buyerId).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Product requireProduct(long productId) throws SQLException {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new ValidationException("Product not found");
        }
        return product;
    }
}