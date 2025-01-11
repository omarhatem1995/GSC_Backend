package com.gsc.gsc.user.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserCreateDTO implements Serializable {
    private String phone;
    private String name;
    private String mail;
    private String password;
    private String address;
    private String accountType;

}