package com.ayeshamart.model;

/**
 * Aggregated figures for the Seller Dashboard (Phase 6). Every number is
 * computed from the database for one seller only: products they own and
 * orders that contain at least one of their products.
 */
public class SellerStats {

    private long totalProducts;
    private long activeProducts;
    private long totalIncomingOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(long activeProducts) {
        this.activeProducts = activeProducts;
    }

    public long getTotalIncomingOrders() {
        return totalIncomingOrders;
    }

    public void setTotalIncomingOrders(long totalIncomingOrders) {
        this.totalIncomingOrders = totalIncomingOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getConfirmedOrders() {
        return confirmedOrders;
    }

    public void setConfirmedOrders(long confirmedOrders) {
        this.confirmedOrders = confirmedOrders;
    }

    public long getShippedOrders() {
        return shippedOrders;
    }

    public void setShippedOrders(long shippedOrders) {
        this.shippedOrders = shippedOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }
}