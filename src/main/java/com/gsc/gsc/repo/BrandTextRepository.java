package com.gsc.gsc.repo;

import com.gsc.gsc.model.BrandText;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandTextRepository extends JpaRepository<BrandText, Integer> {


    Optional<BrandText> findByBrandIdAndLangId(Integer brandId,Integer langId);
}