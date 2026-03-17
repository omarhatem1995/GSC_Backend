package com.gsc.gsc.configurations.controller;

import com.gsc.gsc.configurations.dto.CreateManufacturerDTO;
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
@RequestMapping("/manufacturer")
public class ManufacturerController {

    @Autowired
    ConfigurationService configurationService;

    @PostMapping("")
    public ResponseEntity addManufacturers(@RequestHeader (value = "Authorization") String token,
                                           @ModelAttribute CreateManufacturerDTO createManufacturerDTO){
        return configurationService.addManufacturer(token,createManufacturerDTO);
    }
}
