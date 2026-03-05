package com.gsc.gsc.user.dto;


import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String phone;
    private String name;
    private String token;
    private String taxCard;
    private String commercialLicense;
    private String mailBox;
    private String age;
    private String address;
    private Boolean isVerified;
    private String profileImage;
    private String commercialRegistry;
    private String establishmentRegistration;
    private String mail;
    private Integer customerType;
    private Integer notifications;
    private String password;
    private String newPassword;
    private String verificationOTP;

}