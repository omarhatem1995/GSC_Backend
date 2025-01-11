package com.gsc.gsc.notification.controller;

import com.gsc.gsc.models.service.serviceImplementation.ModelService;
import com.gsc.gsc.notification.service.serviceImplemetation.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @GetMapping("all")
    public ResponseEntity findAllUsersNotifications(@RequestHeader("Authorization") String token ) {
        return notificationService.findAllUsersNotifications(token);
    }

}