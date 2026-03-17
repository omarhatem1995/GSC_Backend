package com.gsc.gsc.user.service.serviceInterface;

import com.gsc.gsc.model.User;
import com.gsc.gsc.user.dto.UserCreateDTO;
import com.gsc.gsc.user.dto.UserDTO;
import org.springframework.http.ResponseEntity;

public interface IUserService <T> {
    User getById(Integer id);
//        ResponseEntity<T> create(UserCreateDTO dto);
    ResponseEntity<T> update(String token, UserDTO dto);
    ResponseEntity<T> delete(Integer id);
}