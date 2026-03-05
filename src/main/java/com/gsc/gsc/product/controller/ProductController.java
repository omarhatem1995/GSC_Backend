package com.gsc.gsc.product.controller;

import com.gsc.gsc.model.OeNumber;
import com.gsc.gsc.model.PagingClass;
import com.gsc.gsc.product.dto.ProductDTO;
import com.gsc.gsc.product.service.serviceImplementation.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.io.IOException;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity addProduct(@RequestHeader("Authorization") String token ,@ModelAttribute ProductDTO dto) throws IOException {
        return productService.create(token,dto);
    }
    @PutMapping("admin/{productId}")
    public ResponseEntity updateProduct(@RequestHeader("Authorization") String token,
                                        @RequestBody ProductDTO productDTO,
                                        @PathVariable Integer productId) {
        return productService.update(token, productId,productDTO );
    }
    @GetMapping("admin/store/{id}")
    public ResponseEntity getProductsForStore(@RequestHeader("Authorization") String token ,@PathVariable Integer id) {
        return productService.findProductsForStoreId(token,id);
    }
    @PostMapping("")
    public ResponseEntity getProducts(@RequestHeader("Authorization") String token,
                                      @RequestHeader(value = "Accept-Language",required = false) String langId,
                                      @RequestBody PagingClass request) {
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        return productService.getProducts(token, getLangId(langId), pageable);
    }
    @PostMapping("products")
    public ResponseEntity getProductsWithCount(@RequestHeader("Authorization") String token,
                                      @RequestHeader("Accept-Language") String langId,
                                      @RequestBody PagingClass request) {
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        return productService.getProductsWithCount(token, getLangId(langId), pageable);
    }


    @PostMapping("/by_brand_id")
    public ResponseEntity<?> getProductsByManufacturerBrandId(
            /*@RequestHeader("Authorization") String token,*/
            @RequestParam("brandId") Integer brandId,
            @RequestBody PagingClass request
    ) {
        String token = "";
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        return productService.getProductsByManufacture(token, brandId, pageable);
    }

    @GetMapping("{productId}")
    public ResponseEntity findProductDetails(@RequestHeader("Authorization") String token,
                                             @RequestHeader("Accept-Language") String langId,
                                             @PathVariable ("productId") Integer productId){
        return productService.findProductDetailsById(token, getLangId(langId),productId);
    }


    @PostMapping("oe_number")
    public ResponseEntity getProductCompatibleOENumber(@RequestBody OeNumber oeNumber) {
        return productService.findOENumbersByBrandAndProductIds(oeNumber);
    }

/*    @PostMapping("comp_vehicles")
    public ResponseEntity getProductCompatibleVehicles(@RequestBody OeNumber oeNumber) {
        return productService.findCompatibleVehiclesByBrandAndProductIds(oeNumber);
    }*/


//        return userService.addToFavourite(token,invoiceProductRepository.getProductId());
}