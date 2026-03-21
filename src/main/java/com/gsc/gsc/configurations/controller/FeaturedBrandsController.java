package com.gsc.gsc.configurations.controller;

import com.gsc.gsc.configurations.dto.FeaturedBrandRequest;
import com.gsc.gsc.configurations.service.serviceImplementation.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/configurations")
public class FeaturedBrandsController {

    @Autowired
    ConfigurationService configurationService;

    @PutMapping("/featuredBrands")
    public ResponseEntity<?> setFeaturedBrands(
            @RequestHeader("Authorization") String token,
            @RequestBody List<FeaturedBrandRequest> requests) {
        return configurationService.setFeaturedBrands(token, requests);
    }
}
