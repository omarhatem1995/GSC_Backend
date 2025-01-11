package com.gsc.gsc.repo;

import com.gsc.gsc.model.BillType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillTypeRepository extends JpaRepository<BillType, Integer> {
    Optional<BillType> findBillTypeByCode(String code);
    Optional<BillType> findBillTypeById(Integer billId);
}