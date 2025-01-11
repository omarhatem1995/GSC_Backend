package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductTypeRepository extends JpaRepository<ProductType, Integer> {
    List<ProductType> findAllById(Integer id);

}