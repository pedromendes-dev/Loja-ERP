package com.example.erp.service.sales;

public class SaleItem {
    private Integer productId;
    private int quantity;

    public SaleItem() {}

    public SaleItem(Integer productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
