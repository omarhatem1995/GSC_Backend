package com.gsc.gsc.configurations.controller;

import com.gsc.gsc.configurations.dto.CreateManufacturerDTO;
import com.gsc.gsc.configurations.service.serviceImplementation.ConfigurationService;
import com.gsc.gsc.model.PagingClass;
import com.gsc.gsc.product.dto.ManufacturerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cms")
public class ConfigurationController {

    @Autowired
    ConfigurationService configurationService;

    @GetMapping("models")
    public ResponseEntity findModels(){
        return configurationService.findAllModels();
    }

    @GetMapping("brands")
    public ResponseEntity findBrands(@RequestHeader("Accept-Language")String langId ){
        return configurationService.findAllBrands(getLangId(langId));
    }
    @GetMapping("allBrands")
    public ResponseEntity findAllBrands(@RequestHeader(value = "Accept-Language",required = false)String langId ){
        return configurationService.getBrandsWithModelsAndColors(getLangId(langId));
    }

    @GetMapping("allSellerBrands")
    public ResponseEntity findAllManufacturers(@RequestHeader(value = "Accept-Language",required = false)String langId ){
        return configurationService.getAllManufacturers(getLangId(langId));
    }

}
