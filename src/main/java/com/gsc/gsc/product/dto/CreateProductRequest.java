package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    private String code;
    private String productNameEn;
    private String productNameAr;
    private String productDescriptionEn;
    private String productDescriptionAr;
    private Double price;
    private Double cost;
    private Integer discountId;
    private Integer promoId;
    private MultipartFile image;
    private List<ManufacturerRequest> manufacturers;
    private List<String> oeNumber;
    private List<VehicleCompatibilityRequest> vehicles;

}
