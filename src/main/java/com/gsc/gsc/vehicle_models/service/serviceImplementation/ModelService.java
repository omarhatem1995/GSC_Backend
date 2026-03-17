package com.gsc.gsc.vehicle_models.service.serviceImplementation;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.model.*;
import com.gsc.gsc.vehicle_models.dto.AddModelDTO;
import com.gsc.gsc.vehicle_models.service.serviceInterface.IModelService;
import com.gsc.gsc.product.dto.ProductDTO;
import com.gsc.gsc.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ModelService implements IModelService {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private BrandRepository brandRepository;


    @Override
    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    @Override
    public ResponseEntity create(String token, ProductDTO dto) {
        return null;
    }

    @Override
    public ResponseEntity<?> getModels(String token) {
        return null;
    }

    @Override
    public ResponseEntity<?> getAllModelsByBrandId(Integer brandId) {
        return ResponseEntity.ok(modelRepository.findAllByBrandId(brandId));
    }
    public ResponseEntity<?> getAllModels() {
        return ResponseEntity.ok(modelRepository.findAll());
    }

    @Override
    public ResponseEntity update(String token, Integer id, CarDTO dto) {
        return null;
    }

    @Override
    public ResponseEntity delete(Integer id) {
        return null;
    }

    public ResponseEntity addModel(AddModelDTO addModelDTO) {
        ReturnObject returnObject = new ReturnObject();
        Model model = new Model();
        Optional<Brand> brandOptional = brandRepository.findById(addModelDTO.getBrandId());
        if(!brandOptional.isPresent()){
            returnObject.setMessage("No Brand Id found");
            returnObject.setStatus(false);
            returnObject.setData(addModelDTO);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if(addModelDTO.getCreationYear() == null){
            returnObject.setMessage("Creation year can't be empty");
            returnObject.setStatus(false);
            returnObject.setData(addModelDTO);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if(addModelDTO.getCode() == null){
            returnObject.setMessage("Code can't be empty");
            returnObject.setStatus(false);
            returnObject.setData(addModelDTO);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        model.setCode(addModelDTO.getCode());
        model.setCreationYear(addModelDTO.getCreationYear());
        model.setBrandId(addModelDTO.getBrandId());
        modelRepository.save(model);
        returnObject.setData(model);
        returnObject.setStatus(true);
        returnObject.setMessage("Added Successfully");
        return ResponseEntity.status(HttpStatus.OK).body(returnObject);
    }

}
