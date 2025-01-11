package com.gsc.gsc.models.controller;

import com.gsc.gsc.models.dto.AddModelDTO;
import com.gsc.gsc.models.service.serviceImplementation.ModelService;
import com.gsc.gsc.product.dto.ProductDTO;
import com.gsc.gsc.product.service.serviceImplementation.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/model")
public class ModelController {

    @Autowired
    ModelService modelService;

    @GetMapping("{brandId}")
    public ResponseEntity getModels(@PathVariable("brandId") Integer brandId ) {
        return modelService.getAllModelsByBrandId(brandId);
    }
    @GetMapping("models")
    public ResponseEntity getAllModels() {
        return modelService.getAllModels();
    }


    @PostMapping("")
    public ResponseEntity addModels(@RequestBody AddModelDTO addModelDTO){
        return modelService.addModel(addModelDTO);
    }

//        return userService.addToFavourite(token,invoiceProductRepository.getProductId());
}