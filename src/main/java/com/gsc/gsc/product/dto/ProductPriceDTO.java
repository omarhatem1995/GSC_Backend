package com.gsc.gsc.product.dto;

public class ProductPriceDTO {
    private Integer productId;
    private Double price;

    public ProductPriceDTO(Integer productId, Double price) {
        this.productId = productId;
        this.price = price;
    }

    public Integer getProductId() { return productId; }
    public Double getPrice() { return price; }
}