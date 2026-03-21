package com.gsc.gsc.user.dto;

import com.gsc.gsc.model.AdminPermission;
import com.gsc.gsc.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LoginResponseDTO implements Serializable {
    private  Integer id;
    private String name;
    private  String mail;
    private  String phone;
    private  LocalDateTime cookieExpiry;
    private  String token;
    private  Integer points;
    private  Integer accountType;
    private Boolean isVerified;
    private AdminPermission permissions;


//    private Boolean isVerified;

    public LoginResponseDTO(User user)
    {
        //isVerified = user.getIsVerified();
        id = user.getId();
        mail = user.getMail();
        phone = user.getPhone();
        name = user.getName();
        accountType = user.getAccountTypeId();
        isVerified = user.getIsVerified();
        //CustomMapper.INSTANCE.updateLoginResponseDTOFromUser(user,this);

    }

}