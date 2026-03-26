package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompatibleVehiclesDTO {
    private Integer modelId;
    private Integer brandId;
    private String brandName;
    private String brandImageUrl;
    private String vehicleCode;
    private Integer creationYear;
    private Integer yearFrom;
    private Integer yearTo;
    private List<ColorDTO> colors;
}
