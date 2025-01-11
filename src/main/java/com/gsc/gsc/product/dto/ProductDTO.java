package com.gsc.gsc.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {
    Integer id;
    String code;
    Double price;
    Integer discountId;
    Integer promoId;
    List<String> oeNumber;
    String location;
    String info;
    Integer quantity;
    Integer brandId;
    String partNo;
    Integer storeId;
    Double cost;
    List<Integer> sellerBrandsList;
    String productNameEn;
    String productNameAr;
    String productDescriptionEn;
    String productDescriptionAr;
}
