package com.gsc.gsc.vonage;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.user.dto.UserDTO;
import com.gsc.gsc.user.service.servicesImplementation.UserService;
import com.gsc.gsc.utilities.VonageConstants;
import com.vonage.client.VonageClient;
import com.vonage.client.verify.CheckResponse;
import com.vonage.client.verify.VerifyResponse;
import com.vonage.client.verify.VerifyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(VonageController.class);

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody VonageModel vonageModel) {
        VonageClient client = VonageClient.builder().apiKey(VonageConstants.API_KEY).apiSecret(VonageConstants.SECRET_KEY).build();

        VerifyResponse response = client.getVerifyClient().verify("+2"+vonageModel.getPhone(), "Brand");
        ReturnObject returnObject = new ReturnObject();
        UserDTO userDTO = new UserDTO();
        userDTO.setPhone(vonageModel.phone);
        userService.resendOtp(userDTO);
        if (response.getStatus() == VerifyStatus.OK) {
            System.out.printf("RequestID: %s", response.getRequestId());
            returnObject.setMessage("Code Send Successfully");
            returnObject.setStatus(true);
            returnObject.setData(response.getRequestId());
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setMessage("Failed to send Code");
            returnObject.setStatus(false);
            returnObject.setData(response.getErrorText());
            return ResponseEntity.badRequest().body(returnObject);
        }

    }
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VonageModel vonageModel) {
        VonageClient client = VonageClient.builder().apiKey(VonageConstants.API_KEY).apiSecret(VonageConstants.SECRET_KEY).build();
        CheckResponse response = client.getVerifyClient().check(vonageModel.getRequestCode(), vonageModel.getSms());

            ReturnObject returnObject = new ReturnObject();
            if (response.getStatus() == VerifyStatus.OK) {
                userService.verifyUser(vonageModel.phone);
                System.out.printf("RequestID: %s", response.getRequestId());
                returnObject.setMessage("Verification Successful");
                returnObject.setStatus(true);
                returnObject.setData(null);
                return ResponseEntity.ok(returnObject);
            } else {
                returnObject.setMessage("Failed to send Code");
                returnObject.setStatus(false);
                returnObject.setData(response.getErrorText());
                return ResponseEntity.badRequest().body(returnObject);
            }
    }
}