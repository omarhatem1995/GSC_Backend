package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVehicleRepository extends JpaRepository<ProductVehicle, Integer> {

    List<ProductVehicle> findAllByProductId(Integer productId);
    void deleteAllByProductId(Integer productId);
}