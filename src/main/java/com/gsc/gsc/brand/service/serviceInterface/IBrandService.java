package com.gsc.gsc.brand.service.serviceInterface;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IBrandService<T> {
    Optional<Car> getById(Integer id);
    //    ResponseEntity<T> create(T dto);
    public ResponseEntity<?> getBrands(String token, Integer langId);
    ResponseEntity<T> update(String token ,Integer id, CarDTO dto);
    ResponseEntity<T> delete(Integer id);
}