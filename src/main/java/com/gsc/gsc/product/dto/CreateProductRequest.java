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
    private String nameEn;
    private String nameAr;

    private String descriptionEn;
    private String descriptionAr;

    private Double price;
    private Double cost;

    private MultipartFile image;

    private List<ManufacturerRequest> manufacturers;
    private List<String> oeNumbers;
    private List<VehicleCompatibilityRequest> vehicles;

}