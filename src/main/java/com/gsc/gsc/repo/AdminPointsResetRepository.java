package com.gsc.gsc.repo;

import com.gsc.gsc.model.AdminPointsReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminPointsResetRepository extends JpaRepository<AdminPointsReset, Integer> {

    Optional<AdminPointsReset> findTopByAdminIdAndUserIdOrderByResetAtDesc(Integer adminId, Integer userId);
}
