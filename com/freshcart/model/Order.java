package com.freshcart.model;

import com.freshcart.util.JsonUtil;
import java.util.List;

public class Order {

    private int id;
    private String orderCode;
    private int userId;
    private String customerName;
    private String phone;
    private String address;
    private String deliveryDate;
    private String deliverySlot;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private String placedAt;
    private List<OrderItem> items;

    public Order() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliverySlot() { return deliverySlot; }
    public void setDeliverySlot(String deliverySlot) { this.deliverySlot = deliverySlot; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getPlacedAt() { return placedAt; }
    public void setPlacedAt(String placedAt) { this.placedAt = placedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String toJson() {
        StringBuilder itemsJson = new StringBuilder("[");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                itemsJson.append(items.get(i).toJson());
                if (i < items.size() - 1) itemsJson.append(",");
            }
        }
        itemsJson.append("]");

        return "{"
                + "\"orderId\":" + JsonUtil.quote(orderCode) + ","
                + "\"placedAt\":" + JsonUtil.quote(placedAt) + ","
                + "\"customer\":{"
                + "\"name\":" + JsonUtil.quote(customerName) + ","
                + "\"phone\":" + JsonUtil.quote(phone) + ","
                + "\"address\":" + JsonUtil.quote(address)
                + "},"
                + "\"deliveryDate\":" + JsonUtil.quote(deliveryDate) + ","
                + "\"deliverySlot\":" + JsonUtil.quote(deliverySlot) + ","
                + "\"subtotal\":" + subtotal + ","
                + "\"delivery\":" + deliveryFee + ","
                + "\"total\":" + total + ","
                + "\"items\":" + itemsJson
                + "}";
    }
}