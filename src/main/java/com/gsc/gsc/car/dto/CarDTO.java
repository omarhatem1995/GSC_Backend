package com.gsc.gsc.car.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
    // populated at service layer, not part of DB query
    private String userName;

    // Constructor used by JPQL queries — must match field order exactly
    public CarDTO(Integer id, String plateNumber, String licenseNumber, String color,
                  String coveredKilos, Integer property, Byte isPremium, String creationYear,
                  String date, String details, String notes, Byte isActivated,
                  String expirationDate, Integer userId, Integer modelId, String modelCode,
                  String chassisNumber, Integer brandId, Integer createdBy) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.licenseNumber = licenseNumber;
        this.color = color;
        this.coveredKilos = coveredKilos;
        this.property = property;
        this.isPremium = isPremium;
        this.creationYear = creationYear;
        this.date = date;
        this.details = details;
        this.notes = notes;
        this.isActivated = isActivated;
        this.expirationDate = expirationDate;
        this.userId = userId;
        this.modelId = modelId;
        this.modelCode = modelCode;
        this.chassisNumber = chassisNumber;
        this.brandId = brandId;
        this.createdBy = createdBy;
    }
}
