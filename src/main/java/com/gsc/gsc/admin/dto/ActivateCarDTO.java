package com.gsc.gsc.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivateCarDTO {
    Integer carId;
    Byte isActivated;
}
