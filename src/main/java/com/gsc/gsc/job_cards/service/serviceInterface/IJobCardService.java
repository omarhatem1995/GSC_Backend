package com.gsc.gsc.job_cards.service.serviceInterface;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.job_cards.dto.JobCardsDTO;
import com.gsc.gsc.model.Car;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IJobCardService<T> {
    Optional<Car> getById(Integer id);
    //    ResponseEntity<T> create(T dto);
    ResponseEntity<T> update(String token , JobCardsDTO dto);
    ResponseEntity<T> delete(Integer id);
}