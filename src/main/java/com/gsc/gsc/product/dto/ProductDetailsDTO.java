// ProductDetailsDTO.java
package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailsDTO {
    private Integer id;
    private String code;
    private String nameEn;
    private String nameAr;
    private String description;
    private String imageUrl;

    private List<CompatibleVehiclesDTO> compatibleVehicles;
    private List<String> oeNumbers;
    private List<ProductManufacturerDTO> stores;

    // Getters and setters
}