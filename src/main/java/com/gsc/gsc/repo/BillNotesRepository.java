package com.gsc.gsc.repo;

import com.gsc.gsc.model.BillNotes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillNotesRepository extends JpaRepository<BillNotes, Integer> {
    List<BillNotes> findAllByBillId(Integer billId);
}