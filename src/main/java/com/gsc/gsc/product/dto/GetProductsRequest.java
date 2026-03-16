package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProductsRequest {

    private Integer brandId;
    private String modelCode;
    private Integer yearFrom;
    private Integer yearTo;
    private String oeNumber;
    private int page = 0;
    private int size = 20;
}