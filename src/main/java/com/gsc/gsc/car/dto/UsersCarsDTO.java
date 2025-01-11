package com.gsc.gsc.car.dto;

import com.gsc.gsc.model.Car;
import com.gsc.gsc.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.sql.Timestamp;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersCarsDTO {
    private int id;
    private String plateNumber;
    private String licenseNumber;
    private String color;
    private String coveredKilos;
    private Byte isPremium;
    private String date;
    private Integer creationYear;
    private String details;
    private String notes;
    private Byte isActivated;
    private Integer userId;
    private String userName;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String expirationDate;
    private Integer modelId;
    private Integer property;
    private String chassisNumber;
    private Boolean isDeleted;
    private Integer createdBy;
    private String modelCode;

    public UsersCarsDTO(Car car, Optional<Model> byId) {
        this.id = car.getId();
        this.chassisNumber = car.getChassisNumber();
        this.createdBy = car.getCreatedBy();
        this.coveredKilos = car.getCoveredKilos();
        this.licenseNumber = car.getLicenseNumber();
        this.plateNumber = car.getPlateNumber();
        this.color = car.getColor();
        this.details = car.getDetails();
        this.notes = car.getNotes();
        this.isPremium = car.getIsPremium();
        this.isActivated = car.getIsActivated();
        this.date = car.getDate();
        this.userId = car.getUserId();
        this.createdAt = car.getCreatedAt();
        this.updatedAt = car.getUpdatedAt();
        this.expirationDate = car.getExpirationDate();
        this.modelId = car.getModelId();
        this.property = car.getProperty();
        this.isDeleted = car.getIsDeleted();
        if(byId != null && byId.isPresent()) {
            this.creationYear = byId.get().getCreationYear();
            this.modelCode = byId.get().getCode();
        }

    }
    public UsersCarsDTO(Car car) {
        this.id = car.getId();
        this.chassisNumber = car.getChassisNumber();
        this.createdBy = car.getCreatedBy();
        this.coveredKilos = car.getCoveredKilos();
        this.licenseNumber = car.getLicenseNumber();
        this.plateNumber = car.getPlateNumber();
        this.color = car.getColor();
        this.details = car.getDetails();
        this.notes = car.getNotes();
        this.isPremium = car.getIsPremium();
        this.isActivated = car.getIsActivated();
        this.date = car.getDate();
        this.userId = car.getUserId();
        this.userName = userName;
        this.createdAt = car.getCreatedAt();
        this.updatedAt = car.getUpdatedAt();
        this.expirationDate = car.getExpirationDate();
        this.modelId = car.getModelId();
        this.property = car.getProperty();
        this.isDeleted = car.getIsDeleted();
    }
}
