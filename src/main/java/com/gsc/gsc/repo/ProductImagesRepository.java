package com.gsc.gsc.repo;

import com.gsc.gsc.model.CAccountType;
import com.gsc.gsc.model.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImagesRepository extends JpaRepository<ProductImages, Integer> {
    List<ProductImages> findAllByProductId(Integer productId);
}