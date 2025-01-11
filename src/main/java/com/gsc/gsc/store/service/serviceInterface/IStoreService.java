package com.gsc.gsc.store.service.serviceInterface;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import com.gsc.gsc.store.dto.StoreDTO;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IStoreService<T> {
    Optional<Car> getById(Integer id);
        ResponseEntity<T> create(String token,StoreDTO dto);
    public ResponseEntity<?> getStores(String token);
    ResponseEntity<T> update(String token ,Integer id, StoreDTO dto);
    ResponseEntity<T> delete(Integer id);
}