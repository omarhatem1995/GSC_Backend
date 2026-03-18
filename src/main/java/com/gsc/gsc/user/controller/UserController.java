package com.gsc.gsc.user.controller;

import com.gsc.gsc.model.FirebaseToken;
import com.gsc.gsc.user.dto.LoginDTO;
import com.gsc.gsc.user.dto.UserDTO;
import com.gsc.gsc.user.security.util.JwtUtil;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("")
    public ResponseEntity addUser(@RequestBody UserDTO userDTO , HttpServletResponse httpRes) {
        return userService.create(userDTO,httpRes);
    }

    @PutMapping("update")
    public ResponseEntity updateUser(@RequestHeader ("Authorization") String token, @RequestBody UserDTO userDTO) {
        return userService.update(token,userDTO);
    }

    @PutMapping("verify")
    public ResponseEntity verifyUser(@RequestBody UserDTO userDTO) {
        return userService.verifyUser(userDTO);
    }

    @PutMapping("admin")
    public ResponseEntity updateUserByAdmin(@RequestHeader ("Authorization") String token, @RequestBody UserDTO userDTO) {
        return userService.updateByAdmin(token,userDTO);
    }

    @GetMapping("")
    public ResponseEntity getUser(@RequestHeader ("Authorization") String token){
        return userService.getUserByToken(token);
    }
    @GetMapping("points")
    public ResponseEntity getPointsUser(@RequestHeader ("Authorization") String token){
        return userService.findPointsByToken(token);
    }

    @PostMapping({"login"})
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginRequestBody
            , HttpServletResponse httpRes) {
        return userService.login(loginRequestBody,httpRes);
    }

    @PostMapping({"update_fcm_token"})
    public ResponseEntity<?> updateFirebaseToken(@RequestHeader ("Authorization") String token
            ,@RequestBody FirebaseToken firebaseToken) {
        return userService.updateFirebaseToken(token,firebaseToken);
    }

    @GetMapping({"/logout"})
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token,
                                    HttpServletResponse httpRes) {
        return userService.logout(token, httpRes);
    }

}