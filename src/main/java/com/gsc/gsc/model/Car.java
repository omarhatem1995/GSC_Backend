package com.gsc.gsc.model;

import com.gsc.gsc.car.dto.CarDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Car {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "plate_number")
    private String plateNumber;
    @Column(name = "license_number")
    private String licenseNumber;
    @Column(name = "color")
    private String color;
    @Column(name = "covered_kilos")
    private String coveredKilos;
    @Column(name = "is_premium")
    private Byte isPremium;
    @Column(name = "date")
    private String date;
    @Column(name = "creation_year")
    private String creationYear;
    @Column(name = "details")
    private String details;
    @Column(name = "notes")
    private String notes;
    @Column(name = "is_activated")
    private Byte isActivated;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "expiration_date")
    private String expirationDate;
    @Column(name = "model_id")
    private Integer modelId;
    @Column(name = "property")
    private Integer property;
    @Column(name = "chassis_number")
    private String chassisNumber;
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    @Column(name = "created_by")
    private Integer createdBy;

    public Car(CarDTO carDTO ,Integer userId) {
    this.isPremium = carDTO.getIsPremium();
    this.creationYear = carDTO.getCreationYear();
    this.color = carDTO.getColor();
    this.coveredKilos = carDTO.getCoveredKilos();
    this.details = carDTO.getDetails();
    this.licenseNumber = carDTO.getLicenseNumber();
    this.plateNumber = carDTO.getPlateNumber();
    this.notes = carDTO.getNotes();
    this.userId = userId;
    this.isActivated = carDTO.getIsActivated();
    this.modelId = carDTO.getModelId();
    this.property = carDTO.getProperty();
    this.chassisNumber = carDTO.getChassisNumber();
    this.isDeleted = false;
    this.createdBy = carDTO.getCreatedBy();
    }

    public Car(CarDTO carDTO) {
        this.isPremium = carDTO.getIsPremium();
        this.creationYear = carDTO.getCreationYear();
        this.color = carDTO.getColor();
        this.coveredKilos = carDTO.getCoveredKilos();
        this.details = carDTO.getDetails();
        this.licenseNumber = carDTO.getLicenseNumber();
        this.plateNumber = carDTO.getPlateNumber();
        this.notes = carDTO.getNotes();
        this.isActivated = carDTO.getIsActivated();
        this.modelId = carDTO.getModelId();
        this.property = carDTO.getProperty();
        this.chassisNumber = carDTO.getChassisNumber();
        this.isDeleted = false;
        this.createdBy = carDTO.getCreatedBy();
    }
}
