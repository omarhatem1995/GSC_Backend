package com.gsc.gsc.repo;

import com.gsc.gsc.model.JobCard;
import com.gsc.gsc.model.JobCardNotes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public interface JobCardNotesRepository extends JpaRepository<JobCardNotes, Integer> {
    List<JobCardNotes> findAllByJobCardId(Integer jobCardId);
    List<JobCardNotes> findAllByJobCardIdAndIsPrivateAndApprovedByCustomerAtIsNullAndCreatedByNot(Integer jobCardId, Boolean isPrivate, Integer createdBy);
}