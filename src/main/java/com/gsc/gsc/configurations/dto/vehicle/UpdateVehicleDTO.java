package com.gsc.gsc.configurations.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVehicleDTO {
    private String nameEn;
    private String nameAr;
    private String descriptionEn;
    private String descriptionAr;
    private String imageUrl;
    private List<VehicleModelDTO> vehicleModelList;
}
