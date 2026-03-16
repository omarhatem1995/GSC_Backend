package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompatibleVehiclesDTO {
    Integer brandId;
    String brandName;
    String brandImageUrl;
    String vehicleCode;
    Integer creationYear;
}
