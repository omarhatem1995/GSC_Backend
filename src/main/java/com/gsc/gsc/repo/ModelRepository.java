package com.gsc.gsc.repo;

import com.gsc.gsc.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Integer> {

    List<Model> findAllByBrandId(Integer integers);
    Optional<Model> findByCodeAndBrandIdAndCreationYear(String code,Integer integers, Integer creationYear);
    List<Model> findAllById(Integer integers);
}