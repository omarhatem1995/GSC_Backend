package com.gsc.gsc.inventory.controller;

import com.gsc.gsc.inventory.dto.CreateProductDTO;
import com.gsc.gsc.inventory.service.ProductServiceV2;
import com.gsc.gsc.model.PagingClass;
import com.gsc.gsc.product.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/productV2")
public class ProductControllerV2 {

    @Autowired
    ProductServiceV2 productService;

    @PostMapping("")
    public ResponseEntity addProduct(@RequestHeader("Authorization") String token,
                                     @ModelAttribute CreateProductDTO dto) throws IOException {
        return productService.create(token, dto);
    }
    @PutMapping("")
    public ResponseEntity updateProduct(@RequestHeader("Authorization") String token,
                                     @ModelAttribute CreateProductDTO dto) throws IOException {
        return productService.update(token, dto);
    }
    @GetMapping("")
    public ResponseEntity getAllProducts(@RequestHeader("Authorization") String token,
                                         @RequestHeader(value = "Accept-Language",required = false) String langId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getProducts(token, getLangId(langId), pageable) ;
    }

    @GetMapping("/by-brand-id")
    public ResponseEntity<?> getProductsByManufacturerBrandId(
            /*@RequestHeader("Authorization") String token,*/
            @RequestParam("brandId") Integer brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = "";
        Pageable pageable = PageRequest.of(page, size);
        return productService.getProductsBySellerBrandId(token, brandId, pageable);
    }

}