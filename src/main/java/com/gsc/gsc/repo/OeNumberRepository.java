package com.gsc.gsc.repo;

import com.gsc.gsc.model.OeNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OeNumberRepository extends JpaRepository<OeNumber, Integer> {

    List<OeNumber> findAllByBrandIdAndProductId(Integer BrandId,Integer productId);
    List<OeNumber> findAllByBrandIdAndProductIdAndOeNumber(Integer BrandId,Integer productId,String oeNumber);
    List<OeNumber> findAllByProductId(Integer productId);
}