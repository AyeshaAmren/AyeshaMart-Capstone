package com.ayeshamart.model;

import java.math.BigDecimal;

/**
 * Aggregated figures for the Admin Dashboard (Phase 7). Every value is
 * computed from the database by AdminService - nothing is hardcoded.
 */
public class AdminStats {

    private int totalUsers;
    private int totalBuyers;
    private int totalSellers;
    private int totalAdmins;
    private int totalProducts;
    private int totalOrders;
    private int pendingOrders;
    private int confirmedOrders;
    private int shippedOrders;
    private int deliveredOrders;
    private int cancelledOrders;
    private BigDecimal totalOrderValue = BigDecimal.ZERO;

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalBuyers() {
        return totalBuyers;
    }

    public void setTotalBuyers(int totalBuyers) {
        this.totalBuyers = totalBuyers;
    }

    public int getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(int totalSellers) {
        this.totalSellers = totalSellers;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public int getConfirmedOrders() {
        return confirmedOrders;
    }

    public void setConfirmedOrders(int confirmedOrders) {
        this.confirmedOrders = confirmedOrders;
    }

    public int getShippedOrders() {
        return shippedOrders;
    }

    public void setShippedOrders(int shippedOrders) {
        this.shippedOrders = shippedOrders;
    }

    public int getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(int deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public int getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(int cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalOrderValue() {
        return totalOrderValue;
    }

    public void setTotalOrderValue(BigDecimal totalOrderValue) {
        this.totalOrderValue = totalOrderValue;
    }
}