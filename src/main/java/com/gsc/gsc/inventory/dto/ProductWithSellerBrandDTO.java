package com.gsc.gsc.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductWithSellerBrandDTO {
    private Integer productId;
    private String productCode;
    private String productNameEn;
    private String productNameAr;
    private String productDescriptionEn;
    private String productDescriptionAr;
    private String partNo;
    private Double price;
    private Double cost;
    private Integer quantity;
    private Integer sellerBrandId;
    private String sellerBrandCode;
    private String sellerBrandNameEn;
    private String sellerBrandNameAr;
    private String sellerBrandImage;

}