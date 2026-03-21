package com.gsc.gsc.admin.dto;

import lombok.Data;

@Data
public class AdminPermissionDTO {
    private Boolean isSuperAdmin;
    private Boolean canManageFeaturedBrands;
    private Boolean canAddPoints;
    private Integer maxPointsPerUser;
    private Boolean canSendNotifications;
    private Boolean canMaintainPrices;
    private Boolean canAddProducts;
    private Boolean canAddVehicles;
    private Boolean canAddModels;
    private Boolean canManageSellerBrands;
    private Double maxBillDiscountPercent;
}
