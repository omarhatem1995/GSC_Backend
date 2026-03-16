package com.gsc.gsc.product.controller;

import com.gsc.gsc.model.Product;
import com.gsc.gsc.product.dto.GetProductsRequest;
import com.gsc.gsc.product.service.serviceImplementation.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/product/v3")
public class ProductV3Controller {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<?> getProductsV3(
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String oeNumber,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        GetProductsRequest request = new GetProductsRequest();
        request.setBrandId(brandId);
        request.setModelCode(modelCode);
        request.setYearFrom(yearFrom);
        request.setYearTo(yearTo);
        request.setOeNumber(oeNumber);
        request.setPage(page);
        request.setSize(size);
        return productService.getProductsV3(request);
    }
    @GetMapping("{productId}")
    public ResponseEntity<?> getProductByIdV3(
            @PathVariable Integer productId
    ) {
        return productService.getProductByIdV3(productId);
    }

}