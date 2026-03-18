package com.gsc.gsc.repo;

import com.gsc.gsc.model.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
    Page<Certificate> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
