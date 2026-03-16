package com.gsc.gsc.repo;

import com.gsc.gsc.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ColorRepository extends JpaRepository<Color, Integer> {
    @Query("SELECT c FROM Color c " +
            "JOIN ModelColor mc ON mc.color.id = c.id " +
            "WHERE mc.model.id = :modelId")
    List<Color> findColorsByModelId(@Param("modelId") Integer modelId);
}