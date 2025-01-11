package com.gsc.gsc.repo;

import com.gsc.gsc.model.Store;
import com.gsc.gsc.model.User;
import com.gsc.gsc.store.dto.GetStoresDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Integer> {
    Optional<Store> findByCode(String storeCode);

    @Query("SELECT NEW com.gsc.gsc.store.dto.GetStoresDTO(s.id,s.createdAt, sd.name, sd.description, si.url,si.storeId) " +
            "FROM Store s " +
            "JOIN StoreText sd ON s.id = sd.storeId " +
            "JOIN StoreImage si ON s.id = si.storeId " +
            "WHERE sd.langId = :langId " +
            "ORDER BY s.id ASC")
    List<GetStoresDTO> findFirst4Stores(Integer langId);

}