package com.gsc.gsc.models.dto;

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
