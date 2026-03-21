package com.gsc.gsc.notification.service.serviceImplementation;

import com.gsc.gsc.constants.NotificationTypes;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.model.Notification;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    public ResponseEntity<?> findAllUsersNotifications(String token, int page, int size) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        Integer userId = userService.getUserIdFromToken(token);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage =
                notificationRepository.findAllByUserIdOrderByIdDesc(userId, pageable);

        // Resolve each notification's type to its human-readable display name
        notificationPage.getContent().forEach(n ->
                n.setNotificationType(NotificationTypes.displayName(n.getNotificationType()))
        );

        returnObject.setStatus(true);
        returnObject.setMessage("Success");
        returnObject.setData(notificationPage.getContent());
        returnObject.setTotalPages(notificationPage.getTotalPages());
        returnObject.setTotalCount(notificationPage.getTotalElements());
        return ResponseEntity.ok(returnObject);
    }
}
