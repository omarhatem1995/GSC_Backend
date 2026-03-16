package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManufacturerRequest {

    private Integer sellerBrandId;
    private Double price;
    private Integer quantity;

}