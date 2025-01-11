package com.gsc.gsc.repo;

import com.gsc.gsc.model.JobCardImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JobCardImagesRepository extends JpaRepository<JobCardImages, Integer> {

    @Transactional
    void deleteAllByJobCardId(Integer jobCardId);

    List<JobCardImages> findAllByJobCardId(Integer jobCardId);
}