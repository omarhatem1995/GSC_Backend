package com.gsc.gsc.user.dto;


import lombok.Data;

import java.io.Serializable;

@Data

public class UserCreateResponseDTO implements Serializable {
    private  Integer id;
    private  String username;
    private  String mail;
    private  String phone;
    private  String name;
    private   String verificationToken;
}