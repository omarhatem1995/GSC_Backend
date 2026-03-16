package com.gsc.gsc.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelBrandDTO {

    private Integer id;
    private String code;
    private Integer brandId;
    private String brandNameEn;
    private String brandNameAr;

}