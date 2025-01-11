package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductSellerBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductSellerBrandRepository extends JpaRepository<ProductSellerBrand, Integer> {

    List<ProductSellerBrand> findAllByProductId(Integer productId);

    @Transactional
    void deleteAllByProductId(Integer productId);
}