package com.gsc.gsc.product_model.service.serviceImplementation;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import com.gsc.gsc.vehicle_models.service.serviceInterface.IModelService;
import com.gsc.gsc.product.dto.ProductDTO;
import com.gsc.gsc.repo.ModelRepository;
import com.gsc.gsc.repo.ProductModelRepository;
import com.gsc.gsc.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductModelService {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private ProductModelRepository productModelRepository;

    public ResponseEntity getAllProductModels(){
        return ResponseEntity.ok(productModelRepository.findAll());
    }

}
