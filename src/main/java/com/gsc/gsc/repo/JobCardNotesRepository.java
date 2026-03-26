package com.gsc.gsc.repo;

import com.gsc.gsc.model.JobCard;
import com.gsc.gsc.model.JobCardNotes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public interface JobCardNotesRepository extends JpaRepository<JobCardNotes, Integer> {
    List<JobCardNotes> findAllByJobCardId(Integer jobCardId);
    List<JobCardNotes> findAllByJobCardIdAndIsPrivateAndApprovedByCustomerAtIsNullAndCreatedByNot(Integer jobCardId, Boolean isPrivate, Integer createdBy);

    // Catches both isPrivate = false AND isPrivate IS NULL (rows inserted before default was enforced)
    @Query("SELECT n FROM JobCardNotes n WHERE n.jobCardId = :jobCardId AND (n.isPrivate = false OR n.isPrivate IS NULL) AND n.approvedByCustomerAt IS NULL AND n.createdBy <> :createdBy")
    List<JobCardNotes> findPendingAdminNotes(@Param("jobCardId") Integer jobCardId, @Param("createdBy") Integer createdBy);
}