package com.gsc.gsc.repo;

import com.gsc.gsc.model.Point;
import com.gsc.gsc.user.dto.PointsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Integer> {
    List<Point> findAllByUserId(Integer userId);
    @Query("SELECT NEW com.gsc.gsc.user.dto.PointsDTO(p.id, p.code, p.userId, p.reason, " +
            "u.name, p.pointsNumber, p.createdAt, p.updatedAt) " +
            "FROM Point p JOIN User u ON p.userId = u.id " +
            "WHERE p.userId = :userId")
    List<PointsDTO> findPointsByUserId(@Param("userId") Integer userId);
}