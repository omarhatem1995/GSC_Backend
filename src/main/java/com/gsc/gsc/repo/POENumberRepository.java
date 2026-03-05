package com.gsc.gsc.repo;

import com.gsc.gsc.inventory.model.POENumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface POENumberRepository extends JpaRepository<POENumber, Long> {
    List<POENumber> findAllByProductBrandSellerBrandId(Long productBrandSellerBrandId);
}