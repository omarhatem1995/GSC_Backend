package com.gsc.gsc.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeaturedBrandResponseDTO {
    private Integer sellerBrandId;
    private Integer displayOrder;
    private String nameEn;
    private String nameAr;
    private String descriptionEn;
    private String descriptionAr;
    private String imageUrl;
    private String code;
}
