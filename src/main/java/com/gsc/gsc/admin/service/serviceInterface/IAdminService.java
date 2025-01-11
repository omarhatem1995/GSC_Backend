package com.gsc.gsc.admin.service.serviceInterface;

import com.gsc.gsc.admin.dto.ActivateCarDTO;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IAdminService<T> {
    Optional<Car> getById(Integer id);
    //    ResponseEntity<T> create(T dto);
    public ResponseEntity<?> getCars(String token);
    ResponseEntity<T> update(String token ,Integer id, CarDTO dto);
    ResponseEntity<T> activateCar(String token , ActivateCarDTO activateCarDTO);
    ResponseEntity<T> delete(Integer id);
}