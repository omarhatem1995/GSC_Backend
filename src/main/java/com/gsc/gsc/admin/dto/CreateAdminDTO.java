package com.gsc.gsc.admin.dto;

import lombok.Data;

@Data
public class CreateAdminDTO {
    private String name;
    private String phone;
    private String password;
    private String mail;
    private AdminPermissionDTO permissions;
}
