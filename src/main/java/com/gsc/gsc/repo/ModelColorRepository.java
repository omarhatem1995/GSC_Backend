package com.gsc.gsc.repo;

import com.gsc.gsc.model.ModelColor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelColorRepository extends JpaRepository<ModelColor, Integer> {
    List<ModelColor> findAllByModel_Id(Integer modelId);
}