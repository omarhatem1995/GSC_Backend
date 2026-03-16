package com.gsc.gsc.configurations.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVehicleDTO {
    String code;
    String nameEn;
    String nameAr;
    String descriptionEn;
    String descriptionAr;
    List<VehicleModelDTO> vehicleModelList;
}
