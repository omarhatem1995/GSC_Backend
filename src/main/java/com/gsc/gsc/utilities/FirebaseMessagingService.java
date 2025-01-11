package com.gsc.gsc.utilities;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService {

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    public String sendNotification(NotificationMessage notificationMessage){
        Notification notification = Notification.builder()
                .setTitle(notificationMessage.getTitle())
                .setBody(notificationMessage.getBody())
                .setImage(notificationMessage.getImage())
                .build();


        Message message = Message.builder().setToken(notificationMessage.getRecToken())
                .setNotification(notification).putAllData(notificationMessage.getData())
                .build();

        try{
            firebaseMessaging.send(message);
            return "Success";
        }catch (FirebaseMessagingException firebaseMessagingException){
            firebaseMessagingException.printStackTrace();
            return "Failed";
        }
    }
}
