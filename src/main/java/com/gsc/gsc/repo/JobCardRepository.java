package com.gsc.gsc.repo;

import com.gsc.gsc.model.JobCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobCardRepository extends JpaRepository<JobCard, Integer> {
    List<JobCard> findAllByUserId(Integer userId);
    Optional<JobCard> findByCode(String code);

    Page<JobCard> findAll(Pageable pageable);
    Page<JobCard> findByCodeContainingIgnoreCase(
            String code,
            Pageable pageable
    );
    @Query("SELECT j FROM JobCard j ORDER BY j.id DESC")
    JobCard findLatestJobCard();

    JobCard findFirstByOrderByIdDesc();
    Page<JobCard> findAllByUserId(Integer userId, Pageable pageable);

    Page<JobCard> findByUserIdAndCodeContainingIgnoreCase(Integer userId, String code, Pageable pageable);
}