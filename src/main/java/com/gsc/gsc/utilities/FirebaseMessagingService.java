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
            String messageId = firebaseMessaging.send(message);
            System.out.println("[FCM] Sent successfully | messageId: " + messageId + " | token: " + notificationMessage.getRecToken());
            return messageId;
        }catch (FirebaseMessagingException e){
            System.err.println("[FCM] Send failed | token: " + notificationMessage.getRecToken() + " | error: " + e.getMessagingErrorCode() + " - " + e.getMessage());
            return "Failed";
        }
    }
}
