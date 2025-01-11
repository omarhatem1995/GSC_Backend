package com.gsc.gsc;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gsc.gsc.utilities.FontLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.awt.*;
import java.io.IOException;

@SpringBootApplication
@EnableTransactionManagement
public class GscApplication {

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("firebase-service-account.json")
                        .getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials).build();

        return FirebaseMessaging.getInstance(FirebaseApp.initializeApp(firebaseOptions,"gsc-application"));
    }

    public static void main(String[] args) {
        SpringApplication.run(GscApplication.class, args);
        try {
            Font font =  FontLoader.loadArabicFont();
            System.out.println("Font loaded: " + font.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        }
    }

}
