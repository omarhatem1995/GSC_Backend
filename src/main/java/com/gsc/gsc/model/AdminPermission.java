package com.gsc.gsc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "admin_permission", schema = "gsc", catalog = "")
public class AdminPermission {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "admin_id", nullable = false, unique = true)
    private Integer adminId;

    /** Super admins bypass all permission checks and can manage other admins' permissions */
    @Column(name = "is_super_admin", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isSuperAdmin = false;

    @Column(name = "can_manage_featured_brands", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canManageFeaturedBrands = false;

    @Column(name = "can_add_points", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canAddPoints = false;

    /** Max total points this admin can give to any single user before reset. Null = unlimited (if canAddPoints = true) */
    @Column(name = "max_points_per_user")
    private Integer maxPointsPerUser;

    @Column(name = "can_send_notifications", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canSendNotifications = false;

    @Column(name = "can_maintain_prices", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canMaintainPrices = false;

    @Column(name = "can_add_products", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canAddProducts = false;

    @Column(name = "can_add_vehicles", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canAddVehicles = false;

    @Column(name = "can_add_models", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canAddModels = false;

    @Column(name = "can_manage_seller_brands", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canManageSellerBrands = false;

    /** Maximum discount % this admin can apply per bill. Null = no discount allowed */
    @Column(name = "max_bill_discount_percent")
    private Double maxBillDiscountPercent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
