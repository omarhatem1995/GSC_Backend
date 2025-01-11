package com.gsc.gsc.brand.controller;

import com.gsc.gsc.brand.service.serviceImplementation.BrandService;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.car.service.serviceImplementation.CarService;
import com.gsc.gsc.user.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    BrandService brandService;
    @Autowired
    JwtUtil jwtUtil;

    @GetMapping({""})
    public ResponseEntity<?> getBrands(@RequestHeader("Authorization") String token , @RequestHeader("Accept-Language")String langId ) {
        return brandService.getBrands(token, getLangId(langId));
    }
}