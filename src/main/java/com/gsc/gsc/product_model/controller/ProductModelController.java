package com.gsc.gsc.product_model.controller;

import com.gsc.gsc.models.service.serviceImplementation.ModelService;
import com.gsc.gsc.product_model.service.ProductModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/product_model")
public class ProductModelController {

    @Autowired
    ProductModelService productModelService;

    @GetMapping("")
    public ResponseEntity getProductModels() {
        return productModelService.getAllProductModels();
    }


//        return userService.addToFavourite(token,invoiceProductRepository.getProductId());
}