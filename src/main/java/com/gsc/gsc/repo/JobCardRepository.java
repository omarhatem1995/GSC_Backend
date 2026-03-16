package com.gsc.gsc.repo;

import com.gsc.gsc.model.JobCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query(
            "SELECT j FROM JobCard j " +
                    "WHERE (:carId IS NULL OR j.carId = :carId) " +
                    "AND (:search = '' OR LOWER(j.code) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<JobCard> findJobCards(
            @Param("carId") Integer carId,
            @Param("search") String search,
            Pageable pageable
    );
    JobCard findFirstByOrderByIdDesc();
    Page<JobCard> findAllByUserId(Integer userId, Pageable pageable);

    Page<JobCard> findByUserIdAndCodeContainingIgnoreCase(Integer userId, String code, Pageable pageable);

    Page<JobCard> findByUserIdAndCarId(Integer userId, Integer carId, Pageable pageable);

    Page<JobCard> findByUserIdAndCarIdAndCodeContainingIgnoreCase(Integer userId, Integer carId, String code, Pageable pageable);
}