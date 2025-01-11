package com.gsc.gsc.product.service.serviceInterface;

import com.gsc.gsc.model.Car;
import com.gsc.gsc.product.dto.ProductDTO;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IProductService<T> {
    Optional<Car> getById(Integer id);
    ResponseEntity<T> create(String token , ProductDTO dto);
//    ResponseEntity<?> getProducts(String token,Integer langId);
    ResponseEntity<T> update(String token ,Integer id, ProductDTO dto);
    ResponseEntity<T> delete(Integer id);
}