package com.gsc.gsc.model;

import com.gsc.gsc.point.dto.AddPointsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "code")
    private String code;
    @Basic
    @Column(name = "user_id")
    private int userId;
    @Basic
    @Column(name = "reason")
    private String reason;
    @Basic
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "points_number")
    private Integer pointsNumber;
    @Basic
    @Column(name = "created_at")
    private String createdAt;
    @Basic
    @Column(name = "updated_at")
    private String updatedAt;

    public Point(AddPointsDTO addPointsDTO) {
        this.pointsNumber = addPointsDTO.getPoints();
        this.code = "";
        this.reason = addPointsDTO.getReason();
        this.userId = addPointsDTO.getUserId();
        this.createdBy = ADMIN_TYPE;
    }
}
