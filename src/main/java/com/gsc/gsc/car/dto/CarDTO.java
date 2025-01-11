package com.gsc.gsc.car.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarDTO {
    private Integer id;
    private String plateNumber;
    private String licenseNumber;
    private String color;
    private String coveredKilos;
    private Integer property;
    private Byte isPremium;
    private String creationYear;
    private String date;
    private String details;
    private String notes;
    private Byte isActivated;
    private String expirationDate;
    private Integer userId;
    private Integer modelId;
    private String modelCode;
    private String chassisNumber;
    private Integer brandId;
    private Integer createdBy;
}
