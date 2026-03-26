package com.gsc.gsc.repo;

import com.gsc.gsc.model.JobCardProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface JobCardProductRepository extends JpaRepository<JobCardProduct, Integer> {
    Optional<List<JobCardProduct>> findAllByJobCardId(Integer jobCardId);
    Optional<List<JobCardProduct>> findAllByJobCardIdAndCustomerApprovedAt(Integer jobCardId, Timestamp customerApprovedAt);
    List<JobCardProduct> findAllByJobCardIdAndProductIdIsNotNull(Integer jobCardId);
    Optional<JobCardProduct> findByJobCardIdAndProductId(Integer jobCardId, Integer productId);

    @Transactional
    void deleteAllByJobCardId(Integer jobCardId);

    @Transactional
    void deleteAllByJobCardIdAndProductIdIsNull(Integer jobCardId);
}