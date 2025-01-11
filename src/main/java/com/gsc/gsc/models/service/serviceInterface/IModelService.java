package com.gsc.gsc.models.service.serviceInterface;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import com.gsc.gsc.product.dto.ProductDTO;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IModelService<T> {
    Optional<Car> getById(Integer id);
    ResponseEntity<T> create(String token , ProductDTO dto);
    ResponseEntity<?> getModels(String token);
    ResponseEntity<?> getAllModelsByBrandId(Integer brandId);
    ResponseEntity<T> update(String token ,Integer id, CarDTO dto);
    ResponseEntity<T> delete(Integer id);
}