package com.gsc.gsc.vonage.controller;

import com.gsc.gsc.user.dto.UserDTO;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import com.gsc.gsc.vonage.dto.VonageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "otp")
public class VonageController {

    @Autowired
    UserService userService;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody VonageModel vonageModel) {
        UserDTO userDTO = new UserDTO();
        userDTO.setPhone(vonageModel.getPhone());
        return userService.resendOtp(userDTO);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VonageModel vonageModel) {
        UserDTO userDTO = new UserDTO();
        userDTO.setPhone(vonageModel.getPhone());
        userDTO.setVerificationOTP(vonageModel.getSms()); // sms field carries the OTP code the user entered
        return userService.verifyUser(userDTO);
    }
}
