package com.gsc.gsc.vehicle_models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddModelDTO {
    String code;
    Integer creationYear;
    Integer brandId;

}
