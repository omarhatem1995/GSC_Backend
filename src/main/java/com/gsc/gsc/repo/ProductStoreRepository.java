package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductStoreRepository extends JpaRepository<ProductStore, Integer> {
    Optional<List<ProductStore>> findAllByStoreId(Integer storeId);
    Optional<List<ProductStore>> findAllByProductId(Integer productId);

    Optional<ProductStore> findByProductId(Integer productId);
}