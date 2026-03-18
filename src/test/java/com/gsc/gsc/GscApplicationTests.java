package com.gsc.gsc;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class GscApplicationTests {

    // Mock Firebase so it doesn't try to load gsc-qatar-firebase.json during tests
    @MockBean
    FirebaseMessaging firebaseMessaging;

    // Mock mail sender so it doesn't need a real SMTP server during tests
    @MockBean
    JavaMailSender javaMailSender;

    @Test
    void contextLoads() {
    }

}
