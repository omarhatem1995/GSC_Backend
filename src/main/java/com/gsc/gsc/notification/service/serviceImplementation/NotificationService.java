package com.gsc.gsc.notification.service.serviceImplementation;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.servicesImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class NotificationService  {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private NotificationRepository notificationRepository;

    public ResponseEntity<?> findAllUsersNotifications(String token) {
        ReturnObject returnObject = new ReturnObject();
        if(token!=null) {
            Integer userId = userService.getUserIdFromToken(token);
            returnObject.setStatus(true);
            returnObject.setMessage("Success");
            returnObject.setData(notificationRepository.findAllByUserId(userId));
            return ResponseEntity.ok(returnObject);
        }else{
            returnObject.setData(new ArrayList<>());
            returnObject.setMessage("Failed");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

}
