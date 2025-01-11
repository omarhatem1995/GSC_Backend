package com.gsc.gsc.email;

import com.gsc.gsc.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/mail")
public class RegistrationController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public String register(@RequestBody UserDTO user) {
        // Generate OTP
        String otp = generateOtp();
        
        // Save user and OTP to the database
        // saveUser(user, otp);
        
        // Send OTP email
        emailService.sendOtpEmail(user.getMail(), otp);

        return "Registration successful. Please check your email for the OTP.";
    }

    private String generateOtp() {
        // Generate a 6-digit OTP
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
