package com.gsc.gsc.repo;

import com.gsc.gsc.model.StoreText;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreDetailsRepository extends JpaRepository<StoreText, Integer> {
    Optional<List<StoreText>> findAllById(Integer storeId);
    Optional<StoreText> findByStoreIdAndLangId(Integer storeId, Integer langId);
}