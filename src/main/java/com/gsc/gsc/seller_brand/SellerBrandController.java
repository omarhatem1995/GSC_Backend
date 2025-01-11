package com.gsc.gsc.seller_brand;

import com.gsc.gsc.store.dto.StoreDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/sellerBrand")
public class SellerBrandController {
    @Autowired
    SellerBrandService sellerBrandService;

    @PostMapping("admin")
    public ResponseEntity addStore(@RequestHeader("Authorization") String token , @RequestBody SellerBrandDTO sellerBrandDTO) {
        return sellerBrandService.createSellerBrand(token,sellerBrandDTO);
    }
}
