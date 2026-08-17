package com.freshcart.model;

import com.freshcart.util.JsonUtil;

public class Product {

    private int id;
    private String name;
    private String category;
    private double price;
    private String unit;
    private String imageUrl;

    public Product() {
    }

    public Product(int id, String name, String category, double price, String unit, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.unit = unit;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"name\":" + JsonUtil.quote(name) + ","
                + "\"category\":" + JsonUtil.quote(category) + ","
                + "\"price\":" + price + ","
                + "\"unit\":" + JsonUtil.quote(unit) + ","
                + "\"image\":" + JsonUtil.quote(imageUrl)
                + "}";
    }
}