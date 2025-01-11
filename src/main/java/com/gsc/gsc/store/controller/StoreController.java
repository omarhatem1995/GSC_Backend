package com.gsc.gsc.store.controller;

import com.gsc.gsc.store.dto.StoreDTO;
import com.gsc.gsc.store.service.serviceImplementation.StoreService;
import com.gsc.gsc.user.dto.LoginDTO;
import com.gsc.gsc.user.dto.UserDTO;
import com.gsc.gsc.user.security.util.JwtUtil;
import com.gsc.gsc.user.service.servicesImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/store")
public class StoreController {

    @Autowired
    StoreService storeService;

    @GetMapping("admin")
    public ResponseEntity getStores(@RequestHeader("Authorization") String token) {
        return storeService.getStores(token);
    }

    @GetMapping("cms/home")
    public ResponseEntity getFirst4Stores() {
        return ResponseEntity.ok(storeService.getFirst4Stores());
    }

    @PostMapping("admin")
    public ResponseEntity addStore(@RequestHeader("Authorization") String token ,@RequestBody StoreDTO storeDTO) {
        return storeService.create(token,storeDTO);
    }
    @PutMapping("admin/{storeId}")
    public ResponseEntity updateStore(@RequestHeader("Authorization") String token,
                                      @RequestBody StoreDTO storeDTO,
                                      @PathVariable Integer storeId) {
        return storeService.update(token, storeId,storeDTO );
    }

//        return userService.addToFavourite(token,invoiceProductRepository.getProductId());
}