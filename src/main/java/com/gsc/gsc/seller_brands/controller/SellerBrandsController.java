package com.gsc.gsc.seller_brands.controller;

import com.gsc.gsc.configurations.service.serviceImplementation.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/sellerBrands")
public class SellerBrandsController {

    @Autowired
    ConfigurationService configurationService;

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedBrands(@RequestHeader("Authorization") String token) {
        return configurationService.getFeaturedBrands();
    }
}
