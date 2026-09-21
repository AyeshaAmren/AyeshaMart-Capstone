package com.ayeshamart.dto;

/**
 * Raw payload of the product add/edit form.
 * Parsing and validation happen in ProductService.
 */
public class ProductForm {

    private final String name;
    private final String description;
    private final String price;
    private final String stockQty;
    private final String category;
    private final String imageUrl;

    public ProductForm(String name, String description, String price,
                       String stockQty, String category, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getStockQty() {
        return stockQty;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}