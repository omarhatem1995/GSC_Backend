package com.gsc.gsc.repo;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface CarRepository extends JpaRepository<Car, Integer> {
    Optional<Car> findCarByUserIdAndId(Integer userId ,Integer integer);
    Optional<List<Car>> findAllByUserId(Integer userId);

    List<Car> findAll();

    @Query("SELECT NEW com.gsc.gsc.car.dto.CarDTO(c.id, c.plateNumber, c.licenseNumber, c.color, " +
            "c.coveredKilos, c.property, c.isPremium, c.creationYear, c.date, c.details, c.notes, c.isActivated, " +
            "CASE WHEN c.isPremium = 1 THEN c.expirationDate ELSE '' END, c.userId, c.modelId, m.code, c.chassisNumber, m.brandId, c.createdBy) " +
            "FROM Car c LEFT JOIN Model m ON c.modelId = m.id WHERE c.isDeleted != true")
    Page<CarDTO> findAllCarsWithModelInfo(Pageable pageable);

    @Query("SELECT NEW com.gsc.gsc.car.dto.CarDTO(c.id, c.plateNumber, c.licenseNumber, c.color, " +
            "c.coveredKilos, c.property, c.isPremium, c.creationYear, c.date, c.details, c.notes, c.isActivated, " +
            "CASE WHEN c.isPremium = 1 THEN c.expirationDate ELSE '' END, c.userId, c.modelId, m.code, c.chassisNumber, m.brandId, c.createdBy) " +
            "FROM Car c LEFT JOIN Model m ON c.modelId = m.id WHERE c.userId=?1 AND c.isDeleted != true")
    List<CarDTO> findAllCarsWithModelInfoByUserId(Integer userId);

    @Query("SELECT NEW com.gsc.gsc.car.dto.CarDTO(c.id, c.plateNumber, c.licenseNumber, c.color, " +
            "c.coveredKilos, c.property, c.isPremium, c.creationYear, c.date, c.details, c.notes, c.isActivated, " +
            "CASE WHEN c.isPremium = 1 THEN c.expirationDate ELSE '' END, c.userId, c.modelId, m.code, c.chassisNumber, m.brandId, c.createdBy) " +
            "FROM Car c LEFT JOIN Model m ON c.modelId = m.id " +
            "WHERE c.isDeleted != true " +
            "AND (:userId IS NULL OR c.userId = :userId) " +
            "AND (:search IS NULL OR c.chassisNumber LIKE %:search% OR c.plateNumber LIKE %:search% OR c.licenseNumber LIKE %:search%)")
    Page<CarDTO> findAllCarsWithFilters(
            @Param("userId") Integer userId,
            @Param("search") String search,
            Pageable pageable);

    Optional<Car> findCarByLicenseNumber(String licenseNumber);
    Optional<Car> findCarByChassisNumber(String chassisNumber);


    @Transactional
    @Modifying
    @Query(value = "UPDATE Car c SET c.is_activated = :isActivated, " +
            "c.expiration_date = CASE WHEN :isActivated = 1 AND c.is_premium = 1 THEN :currentDatePlusOneYear ELSE NULL END " +
            "WHERE c.id = :carId", nativeQuery = true)
    void updateActivationStatus(
            @Param("carId") Integer carId,
            @Param("isActivated") Byte isActivated,
            @Param("currentDatePlusOneYear") Timestamp currentDatePlusOneYear
    );





/*    default void activateCarWithExpirationDate(Integer carId) {
        Date currentDatePlusOneYear = Date.from(LocalDateTime.now().plus(1, ChronoUnit.YEARS).atZone(ZoneId.systemDefault()).toInstant());
        updateActivationStatus(carId, (byte) 1, currentDatePlusOneYear);
    }*/


}