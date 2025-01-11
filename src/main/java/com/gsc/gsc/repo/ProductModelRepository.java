package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductModelRepository extends JpaRepository<ProductModel, Integer> {

    List<ProductModel> findAllByProductId(Integer productId);
}