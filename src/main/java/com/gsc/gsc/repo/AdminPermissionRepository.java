package com.gsc.gsc.repo;

import com.gsc.gsc.model.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Integer> {

    Optional<AdminPermission> findByAdminId(Integer adminId);
}
