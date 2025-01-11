package com.gsc.gsc.configurations;

import com.gsc.gsc.model.PagingClass;
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

    @PostMapping("")
    public ResponseEntity getProductsByFilter(@RequestHeader("Authorization") String token,
                                              @RequestHeader("Accept-Language") String langId,
                                              @RequestParam(name = "brandId", required = false) Integer brandId,
                                              @RequestParam(name = "modelId", required = false) Integer modelId,
                                              @RequestParam(name = "creationYear", required = false) Integer creationYear,
                                              @RequestBody PagingClass request) {
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        return configurationService.getProducts(token, getLangId(langId), brandId, modelId,creationYear, pageable);
    }

    @GetMapping("models")
    public ResponseEntity findModels(){
        return configurationService.findAllModels();
    }
    @GetMapping("brands")
    public ResponseEntity findBrands(@RequestHeader("Accept-Language")String langId ){
        return configurationService.findAllBrands(getLangId(langId));
    }

}
