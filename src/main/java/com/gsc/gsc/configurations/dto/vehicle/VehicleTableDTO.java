package com.gsc.gsc.configurations.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleTableDTO {
    private Integer id;
    private String code;
    private String nameEn;
    private String nameAr;
    private String imageUrl;
    private long modelCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
