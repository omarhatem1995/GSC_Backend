package com.gsc.gsc.repo;

import com.gsc.gsc.model.SellerBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerBrandRepository extends JpaRepository<SellerBrand, Integer> {
    Optional<SellerBrand> findById(Integer id);
}