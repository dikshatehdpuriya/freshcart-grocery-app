package com.freshcart.model;

import com.freshcart.util.JsonUtil;

public class OrderItem {

    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private double lineTotal;

    public OrderItem() {
    }

    public OrderItem(int productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.lineTotal = round(price * quantity);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }

    public String toJson() {
        return "{"
                + "\"productId\":" + productId + ","
                + "\"name\":" + JsonUtil.quote(productName) + ","
                + "\"price\":" + price + ","
                + "\"quantity\":" + quantity + ","
                + "\"lineTotal\":" + lineTotal
                + "}";
    }
}