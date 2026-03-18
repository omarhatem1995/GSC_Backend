package com.gsc.gsc.repo;

import com.gsc.gsc.configurations.dto.ModelBrandDTO;
import com.gsc.gsc.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Integer> {
    @Query("SELECT new com.gsc.gsc.configurations.dto.ModelBrandDTO(" +
            "m.id, m.code, b.id, b.nameEn, b.nameAr) " +
            "FROM Model m JOIN Brand b ON m.brandId = b.id " +
            "WHERE m.id IN (" +
            "SELECT MIN(m2.id) FROM Model m2 GROUP BY m2.code, m2.brandId)")
    List<ModelBrandDTO> findDistinctByCode();
    List<Model> findAllByBrandId(Integer integers);
    List<Model> findModelsByBrandId(Integer integers);
    boolean existsByBrandIdAndCodeAndCreationYear(Integer brandId, String code, Integer creationYear);
    List<Model> findAllById(Integer integers);
    long countByBrandId(Integer brandId);
    Optional<Model> findByBrandIdAndCodeAndCreationYear(Integer brandId, String code, Integer creationYear);
}