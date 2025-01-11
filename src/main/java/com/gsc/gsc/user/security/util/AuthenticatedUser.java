package com.gsc.gsc.user.security.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticatedUser {
    private Integer userId;
    private Integer employeeId;
    private Integer customerType;
    private Integer employeeType;


    public AuthenticatedUser(Integer userId, Integer employeeId)
    {
        this.userId = userId;
        this.employeeId =employeeId;
    }
}