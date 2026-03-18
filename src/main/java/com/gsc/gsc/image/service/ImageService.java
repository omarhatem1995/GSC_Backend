package com.gsc.gsc.image.service;

import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

/*    public ResponseEntity<?> uploadImage(String token,Integer imageType,String url){
        Integer userId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();
        if(userId!=null) {
            if (userAdmin.getAccountTypeId() == ADMIN) {

            }
        }

    }*/

}
