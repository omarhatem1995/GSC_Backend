package com.gsc.gsc.repo;

import com.gsc.gsc.model.CAccountType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTypeRepository extends JpaRepository<CAccountType, Integer> {
}