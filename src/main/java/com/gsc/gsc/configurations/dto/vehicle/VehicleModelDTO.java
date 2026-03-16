package com.gsc.gsc.configurations.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleModelDTO {
    String code;
    String nameEn;
    String nameAr;
    List<String> years;
}
