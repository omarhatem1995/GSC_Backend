package com.gsc.gsc.car.service.serviceInterface;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface ICarService<T> {
    Optional<Car> getById(Integer id);
    //    ResponseEntity<T> create(T dto);
    public ResponseEntity<?> getCarsByToken(String token);
    ResponseEntity<T> update(String token ,Integer id, CarDTO dto);
    ResponseEntity<T> delete(String token,Integer id);
}