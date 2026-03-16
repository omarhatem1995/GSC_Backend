package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCompatibilityRequest {

    private Integer modelId;
    private Integer yearFrom;
    private Integer yearTo;

}