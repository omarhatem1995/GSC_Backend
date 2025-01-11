package com.gsc.gsc.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PointsDTO {
    private int id;
    private String code;
    private int userId;
    private String reason;
    private String createdBy;
    private int pointsNumber;
    private String createdAt;
    private String updatedAt;
}
