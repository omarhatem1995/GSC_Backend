package com.gsc.gsc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "admin_points_reset", schema = "gsc", catalog = "")
public class AdminPointsReset {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    /** The admin whose limit is being reset */
    @Column(name = "admin_id", nullable = false)
    private Integer adminId;

    /** The customer user whose points limit is being reset for this admin */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** The super admin who performed the reset */
    @Column(name = "reset_by", nullable = false)
    private Integer resetBy;

    /**
     * Running total of points this admin had given to this user at the moment of reset.
     * Used as a baseline: points given since last reset = currentTotal - pointsBaseline.
     */
    @Column(name = "points_baseline", nullable = false)
    private Integer pointsBaseline;

    @CreationTimestamp
    @Column(name = "reset_at", updatable = false)
    private Timestamp resetAt;
}
