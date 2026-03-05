package com.gsc.gsc.inventory.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateProductDTO {
    Integer brandId;
    Integer productId;
    Integer sellerBrandId;
    Integer storeId;
    Integer createdById;
    String partNo;
    Double price;
    Double cost;
    Integer quantity;
    String productNameEn;
    String productNameAr;
    String productDescriptionEn;
    String productDescriptionAr;
    List<String> oeNumberList;
    MultipartFile image;
}
