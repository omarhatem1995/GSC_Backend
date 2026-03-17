package com.gsc.gsc.brand.service.serviceImplementation;

import com.gsc.gsc.brand.service.serviceInterface.IBrandService;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import com.gsc.gsc.repo.BrandRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BrandService implements IBrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<?> getBrands(String token,Integer langId) {
        return ResponseEntity.ok(brandRepository.findBrandsByLangId(langId));
    }

    @Override
    public ResponseEntity update(String token, Integer id, CarDTO dto) {
        return null;
    }

    @Override
    public ResponseEntity delete(Integer id) {
        return null;
    }
}
