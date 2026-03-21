package com.gsc.gsc.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetProductDTO {
    Long id;
    Integer modelId;
    Integer brandId;
    Integer productId;
    Integer sellerBrandId;
    Integer createdById;
    String partNo;
    Double price;
    Double cost;
    Integer quantity;
    List<String> oeNumberList;


    String createdByName;
    String productCode;
    String productNameEn;
    String productNameAr;
    String productDescriptionEn;
    String productDescriptionAr;
    Integer productTypeId;

    String modelCode;
    String modelNameEn;
    String modelNameAr;

    String brandCode;
    String brandNameEn;
    String brandNameAr;
    String brandDescriptionEn;
    String brandDescriptionAr;
    String brandImageUrl;

    String sellerBrandCode;
    String sellerBrandNameEn;
    String sellerBrandNameAr;
    String sellerBrandDescriptionEn;
    String sellerBrandDescriptionAr;
    String sellerBrandImageUrl;

}
