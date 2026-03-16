package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductOeNumber;
import com.gsc.gsc.product.dto.CompatibleVehiclesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOeNumberRepository extends JpaRepository<ProductOeNumber, Integer> {

    List<ProductOeNumber> findAllByProductId(Integer productId);
}