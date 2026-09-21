package com.ayeshamart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One seller-scoped line from an incoming order (Phase 6). Rows come from
 * order_items joined with orders (buyer + status + date) and products
 * (name, image), always filtered by p.seller_id so a seller only ever sees
 * their own products inside any order.
 */
public class SellerOrderLine {

    private long orderId;
    private long itemId;
    private long productId;
    private String productName;
    private String imageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private String buyerName;
    private LocalDateTime orderDate;
    private String status;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice == null ? BigDecimal.ZERO
                : unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}