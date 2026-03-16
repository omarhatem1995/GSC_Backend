package com.gsc.gsc.repo;

import com.gsc.gsc.brand.dto.BrandDTO;
import com.gsc.gsc.configurations.dto.CBrandDTO;
import com.gsc.gsc.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    boolean existsByCode(String code);
    @Query("SELECT NEW com.gsc.gsc.brand.dto.BrandDTO(" +
            "b.id, b.code, b.imageUrl, " +
            "CASE WHEN :langId = 1 THEN b.nameEn ELSE b.nameAr END, " +
            "CASE WHEN :langId = 1 THEN b.descriptionEn ELSE b.descriptionAr END) " +
            "FROM Brand b")
    Optional<List<BrandDTO>> findBrandsByLangId(Integer langId);
    @Query("SELECT NEW com.gsc.gsc.configurations.dto.CBrandDTO(" +
            "b.id, b.code, b.imageUrl, " +
            "CASE WHEN :langId = 1 THEN b.nameEn ELSE b.nameAr END, " +
            "CASE WHEN :langId = 1 THEN b.descriptionEn ELSE b.descriptionAr END) " +
            "FROM Brand b")
    List<CBrandDTO> findCBrandsByLangId(Integer langId);
    @Query("SELECT NEW com.gsc.gsc.brand.dto.BrandDTO(" +
            "b.id, b.code, b.imageUrl, " +
            "CASE WHEN :langId = 1 THEN b.nameEn ELSE b.nameAr END, " +
            "CASE WHEN :langId = 1 THEN b.descriptionEn ELSE b.descriptionAr END) " +
            "FROM Brand b " +
            "WHERE b.id = :brandId")
    Optional<BrandDTO> findBrandsByLangIdAndBrandId(Integer langId, Integer brandId);

}