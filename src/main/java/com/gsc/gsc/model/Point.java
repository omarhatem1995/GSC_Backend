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
    @Column(name = "code")
    private String code;
    @Column(name = "user_id")
    private int userId;
    @Column(name = "reason")
    private String reason;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "points_number")
    private Integer pointsNumber;
    @Column(name = "operation_type")
    private Integer operationType;
    @Column(name = "created_at")
    private String createdAt;
    @Column(name = "updated_at")
    private String updatedAt;

    public Point(AddPointsDTO addPointsDTO,Integer adminId) {
        this.pointsNumber = addPointsDTO.getPoints();
        this.code = "";
        this.reason = addPointsDTO.getReason();
        this.userId = addPointsDTO.getUserId();
        this.operationType = addPointsDTO.getOperationType();
        this.createdBy = adminId;
    }
}
