package com.gsc.gsc.product.controller;

import com.gsc.gsc.model.OeNumber;
import com.gsc.gsc.model.PagingClass;
import com.gsc.gsc.product.dto.CreateProductRequest;
import com.gsc.gsc.product.dto.ProductDTO;
import com.gsc.gsc.product.service.serviceImplementation.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@RequestHeader("Authorization") String token,
                                        @ModelAttribute CreateProductRequest dto) {
        return productService.createProduct(token, dto);
    }

    @PutMapping(value = "admin/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@RequestHeader("Authorization") String token,
                                           @ModelAttribute ProductDTO productDTO,
                                           @PathVariable Integer productId) {
        return productService.update(token, productId, productDTO);
    }

    @PostMapping("products")
    public ResponseEntity getProductsWithCount(@RequestHeader("Authorization") String token,
                                      @RequestHeader("Accept-Language") String langId,
                                      @RequestBody PagingClass request) {
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        return productService.getProductsWithCount(token, getLangId(langId), pageable);
    }

    @PostMapping("oe_number")
    public ResponseEntity getProductCompatibleOENumber(@RequestBody OeNumber oeNumber) {
        return productService.findOENumbersByBrandAndProductIds(oeNumber);
    }

}
