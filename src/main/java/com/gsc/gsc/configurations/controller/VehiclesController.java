package com.gsc.gsc.configurations.controller;

import com.gsc.gsc.configurations.dto.CreateManufacturerDTO;
import com.gsc.gsc.configurations.dto.vehicle.CreateVehicleDTO;
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
@RequestMapping("/vehicle")
public class VehiclesController {

    @Autowired
    ConfigurationService configurationService;

    @GetMapping("")
    public ResponseEntity getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return configurationService.getVehiclesTable(page, size);
    }

    @PostMapping("")
    public ResponseEntity addVehicles(@RequestHeader (value = "Authorization") String token,
                                           @RequestBody CreateVehicleDTO createVehicleDTO){
        return configurationService.addVehicles(token,createVehicleDTO);
    }
}
