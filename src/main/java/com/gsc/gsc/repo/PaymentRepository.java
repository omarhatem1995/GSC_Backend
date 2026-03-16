package com.gsc.gsc.repo;

import com.gsc.gsc.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.billId = :billId")
    Double getTotalPaid(@Param("billId") Long billId);
    List<Payment> findByBillId(Long billId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.billId = :billId")
    Double getTotalPaidByBillId(@Param("billId") Long billId);

}