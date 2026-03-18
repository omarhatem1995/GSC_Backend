package com.gsc.gsc.fcm.controller;

import com.gsc.gsc.utilities.FirebaseMessagingService;
import com.gsc.gsc.utilities.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
public class FcmController {


    @Autowired
    FirebaseMessagingService firebaseMessagingService;


    @PostMapping("send")
    public ResponseEntity sendNotification(@RequestBody NotificationMessage notificationMessage) {
        return ResponseEntity.ok(firebaseMessagingService.sendNotification(notificationMessage));
    }
}