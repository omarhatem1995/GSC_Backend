package com.gsc.gsc.repo;

import com.gsc.gsc.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByUserIdOrderByIdDesc(Integer userId);
    Page<Notification> findAllByUserIdOrderByIdDesc(Integer userId, Pageable pageable);
}