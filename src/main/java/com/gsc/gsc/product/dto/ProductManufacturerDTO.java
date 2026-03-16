package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductManufacturerDTO {

    private Integer sellerBrandId;
    private String sellerBrandName;
    private String sellerBrandImageUrl;
    private Double price;
    private Integer quantity;
    private String imageUrl;

    public ProductManufacturerDTO(int id, Integer quantity, Double price,String brandName) {
        this.sellerBrandId = id;
        this.quantity = quantity;
        this.price = price;
        this.sellerBrandName = brandName;
    }
}
