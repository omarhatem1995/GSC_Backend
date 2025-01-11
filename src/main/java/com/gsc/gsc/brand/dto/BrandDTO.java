package com.gsc.gsc.brand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO {
    Integer brandId;
    String brandCode;
    String brandImage;
    String brandName;
    String brandDescription;

}
