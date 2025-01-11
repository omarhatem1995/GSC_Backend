package com.gsc.gsc.repo;

import com.gsc.gsc.model.BillProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BillProductRepository extends JpaRepository<BillProduct, Integer> {
    Optional<List<BillProduct>> findAllByProductId(Integer productId);
    Optional<List<BillProduct>> findAllByBillId(Integer billId);

    @Transactional
    void deleteAllByBillId(Integer billId);
}