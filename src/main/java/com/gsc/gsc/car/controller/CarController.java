package com.gsc.gsc.car.controller;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.car.service.serviceImplementation.CarService;
import com.gsc.gsc.user.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user/car")
public class CarController {

    @Autowired
    CarService carService;
    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("")
    public ResponseEntity addCar(@RequestHeader("Authorization") String token ,@RequestBody CarDTO carDTO) {
        return carService.create(token, carDTO);
    }
    @PutMapping("/{id}")
    public ResponseEntity updateCar(@RequestHeader("Authorization") String token,
                                    @PathVariable Integer id, @RequestBody CarDTO carDTO) {
        return carService.update(token,id,carDTO);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity deleteCar(@RequestHeader("Authorization") String token,
                                    @PathVariable Integer id) {
        return carService.delete(token,id);
    }
    @PutMapping("admin/{id}")
    public ResponseEntity updateCarByAdmin(@RequestHeader("Authorization") String token,
                                        @PathVariable Integer id, @RequestBody CarDTO carDTO) {
        return carService.updateCarByAdmin(token,id,carDTO);
    }

    @GetMapping({""})
    public ResponseEntity<?> getCarsByToken(@RequestHeader("Authorization") String token) {
        return carService.getCarsByToken(token);
    }
    @GetMapping({"admin"})
    public ResponseEntity<?> getCarsForAdminByToken(@RequestHeader("Authorization") String token,
                                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return carService.getCarsForAdminByToken(token, page, size);
    }

    @GetMapping({"admin/{id}"})
    public ResponseEntity<?> getCarsForAdminByTokenAndUserId(@RequestHeader("Authorization") String token , @PathVariable ("id") Integer userId) {
        return carService.getCarsForAdminByTokenAndUserId(token,userId);
    }
    @PutMapping({"admin/approve/{carId}"})
    public ResponseEntity<?> approveCarByAdmin(@RequestHeader("Authorization") String token , @PathVariable ("carId") Integer carId) {
        return carService.approveCarByAdmin(token,carId);
    }
  /*


*/
//        return carService.addToFavourite(token,invoiceProductRepository.getProductId());
}