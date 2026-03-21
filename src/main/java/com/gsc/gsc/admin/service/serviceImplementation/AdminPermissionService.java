package com.gsc.gsc.admin.service.serviceImplementation;

import com.gsc.gsc.admin.dto.AdminPermissionDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.model.AdminPermission;
import com.gsc.gsc.model.AdminPointsReset;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.AdminPermissionRepository;
import com.gsc.gsc.repo.AdminPointsResetRepository;
import com.gsc.gsc.repo.PointRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;

@Service
public class AdminPermissionService {

    @Autowired private AdminPermissionRepository adminPermissionRepository;
    @Autowired private AdminPointsResetRepository adminPointsResetRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    // ─────────────────────────────────────────────────────────────
    // Public helpers used by other services
    // ─────────────────────────────────────────────────────────────

    /** Returns the permission record for an admin, or empty if none assigned. */
    public Optional<AdminPermission> getPermission(Integer adminId) {
        return adminPermissionRepository.findByAdminId(adminId);
    }

    /** True if this admin is a super admin (bypasses all checks). */
    public boolean isSuperAdmin(Integer adminId) {
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getIsSuperAdmin()))
                .orElse(false);
    }

    public boolean canManageFeaturedBrands(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanManageFeaturedBrands()))
                .orElse(false);
    }

    public boolean canAddPoints(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanAddPoints()))
                .orElse(false);
    }

    public boolean canSendNotifications(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanSendNotifications()))
                .orElse(false);
    }

    public boolean canMaintainPrices(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanMaintainPrices()))
                .orElse(false);
    }

    public boolean canAddProducts(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanAddProducts()))
                .orElse(false);
    }

    public boolean canAddVehicles(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanAddVehicles()))
                .orElse(false);
    }

    public boolean canAddModels(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanAddModels()))
                .orElse(false);
    }

    public boolean canManageSellerBrands(Integer adminId) {
        if (isSuperAdmin(adminId)) return true;
        return adminPermissionRepository.findByAdminId(adminId)
                .map(p -> Boolean.TRUE.equals(p.getCanManageSellerBrands()))
                .orElse(false);
    }

    /**
     * Checks if admin has exceeded their per-user points limit.
     * Returns null if allowed, or an error message if blocked.
     */
    public String checkPointsLimit(Integer adminId, Integer userId, Integer pointsToAdd) {
        if (isSuperAdmin(adminId)) return null;

        Optional<AdminPermission> permOpt = adminPermissionRepository.findByAdminId(adminId);
        if (permOpt.isEmpty() || !Boolean.TRUE.equals(permOpt.get().getCanAddPoints())) {
            return "You do not have permission to add points";
        }

        Integer maxPointsPerUser = permOpt.get().getMaxPointsPerUser();
        if (maxPointsPerUser == null) return null; // unlimited

        int totalGiven = pointRepository.sumPointsGivenByAdminToUser(adminId, userId);
        int baseline = adminPointsResetRepository
                .findTopByAdminIdAndUserIdOrderByResetAtDesc(adminId, userId)
                .map(AdminPointsReset::getPointsBaseline)
                .orElse(0);

        int givenSinceReset = totalGiven - baseline;
        if (givenSinceReset + pointsToAdd > maxPointsPerUser) {
            int remaining = Math.max(0, maxPointsPerUser - givenSinceReset);
            return "Points limit reached for this user. Remaining: " + remaining + " pts. Contact a super admin to reset.";
        }
        return null;
    }

    /**
     * Returns null if discount is allowed, or an error message if the percent exceeds the admin's limit.
     */
    public String checkDiscountLimit(Integer adminId, Double discountPercent) {
        if (isSuperAdmin(adminId)) return null;
        if (discountPercent == null || discountPercent <= 0) return null;

        Optional<AdminPermission> permOpt = adminPermissionRepository.findByAdminId(adminId);
        if (permOpt.isEmpty()) {
            return "You do not have permission to apply discounts";
        }
        Double maxDiscount = permOpt.get().getMaxBillDiscountPercent();
        if (maxDiscount == null) {
            return "You do not have permission to apply discounts";
        }
        if (discountPercent > maxDiscount) {
            return "Discount exceeds your allowed maximum of " + maxDiscount + "%";
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD endpoints
    // ─────────────────────────────────────────────────────────────

    public ResponseEntity<?> setPermissions(String token, Integer targetAdminId, AdminPermissionDTO dto) {
        ReturnObject returnObject = new ReturnObject();
        Integer requesterId = userService.getUserIdFromToken(token);

        if (!isSuperAdmin(requesterId)) {
            returnObject.setStatus(false);
            returnObject.setMessage("Only super admins can manage permissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        User targetAdmin = userRepository.findUserById(targetAdminId);
        if (targetAdmin == null || targetAdmin.getAccountTypeId() != ADMIN_TYPE) {
            returnObject.setStatus(false);
            returnObject.setMessage("Target user is not an admin");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        AdminPermission permission = adminPermissionRepository
                .findByAdminId(targetAdminId)
                .orElse(new AdminPermission());

        permission.setAdminId(targetAdminId);
        applyDtoToPermission(permission, dto);

        AdminPermission saved = adminPermissionRepository.save(permission);

        returnObject.setStatus(true);
        returnObject.setMessage("Permissions updated successfully");
        returnObject.setData(saved);
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> getPermissions(String token, Integer targetAdminId) {
        ReturnObject returnObject = new ReturnObject();
        Integer requesterId = userService.getUserIdFromToken(token);

        if (!isSuperAdmin(requesterId) && !requesterId.equals(targetAdminId)) {
            returnObject.setStatus(false);
            returnObject.setMessage("Not authorized to view these permissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        Optional<AdminPermission> permOpt = adminPermissionRepository.findByAdminId(targetAdminId);
        returnObject.setStatus(true);
        returnObject.setMessage("Loaded successfully");
        returnObject.setData(permOpt.orElse(null));
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> resetPointsLimit(String token, Integer targetAdminId, Integer userId) {
        ReturnObject returnObject = new ReturnObject();
        Integer superAdminId = userService.getUserIdFromToken(token);

        if (!isSuperAdmin(superAdminId)) {
            returnObject.setStatus(false);
            returnObject.setMessage("Only super admins can reset points limits");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        int currentTotal = pointRepository.sumPointsGivenByAdminToUser(targetAdminId, userId);

        AdminPointsReset reset = new AdminPointsReset();
        reset.setAdminId(targetAdminId);
        reset.setUserId(userId);
        reset.setResetBy(superAdminId);
        reset.setPointsBaseline(currentTotal);
        adminPointsResetRepository.save(reset);

        returnObject.setStatus(true);
        returnObject.setMessage("Points limit reset successfully for admin " + targetAdminId + " / user " + userId);
        returnObject.setData(null);
        return ResponseEntity.ok(returnObject);
    }

    // ─────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────

    private void applyDtoToPermission(AdminPermission p, AdminPermissionDTO dto) {
        if (dto.getIsSuperAdmin() != null)             p.setIsSuperAdmin(dto.getIsSuperAdmin());
        if (dto.getCanManageFeaturedBrands() != null)  p.setCanManageFeaturedBrands(dto.getCanManageFeaturedBrands());
        if (dto.getCanAddPoints() != null)             p.setCanAddPoints(dto.getCanAddPoints());
        if (dto.getMaxPointsPerUser() != null)         p.setMaxPointsPerUser(dto.getMaxPointsPerUser());
        if (dto.getCanSendNotifications() != null)     p.setCanSendNotifications(dto.getCanSendNotifications());
        if (dto.getCanMaintainPrices() != null)        p.setCanMaintainPrices(dto.getCanMaintainPrices());
        if (dto.getCanAddProducts() != null)           p.setCanAddProducts(dto.getCanAddProducts());
        if (dto.getCanAddVehicles() != null)           p.setCanAddVehicles(dto.getCanAddVehicles());
        if (dto.getCanAddModels() != null)             p.setCanAddModels(dto.getCanAddModels());
        if (dto.getCanManageSellerBrands() != null)    p.setCanManageSellerBrands(dto.getCanManageSellerBrands());
        if (dto.getMaxBillDiscountPercent() != null)   p.setMaxBillDiscountPercent(dto.getMaxBillDiscountPercent());
    }
}
