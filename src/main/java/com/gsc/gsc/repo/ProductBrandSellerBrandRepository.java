package com.gsc.gsc.repo;

import com.gsc.gsc.inventory.model.ProductModelSellerBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductBrandSellerBrandRepository extends JpaRepository<ProductModelSellerBrand, Long> {

    Optional<ProductModelSellerBrand> findByProductIdAndModelIdAndSellerBrandId(Integer productId, Integer modelId, Integer sellerBrandId);
}