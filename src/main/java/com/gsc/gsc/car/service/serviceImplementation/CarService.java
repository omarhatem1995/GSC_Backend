package com.gsc.gsc.car.service.serviceImplementation;

import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.car.service.serviceInterface.ICarService;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.model.Car;
import com.gsc.gsc.model.Model;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.CarRepository;
import com.gsc.gsc.repo.ModelRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.constants.UserTypes.USER_TYPE;

@Service
public class CarService implements ICarService {

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelRepository modelRepository;

    @Override
    public Optional<Car> getById(Integer id) {
        return carRepository.findById(id);
    }


    public List<String> checkEmptyFields(CarDTO car) {
        List<String> messages = new ArrayList<>();

        if (car.getPlateNumber() == null || car.getPlateNumber().isEmpty()) {
            messages.add("Plate number is missing");
        }
        if (car.getChassisNumber() == null || car.getChassisNumber().isEmpty()) {
            messages.add("Chassis number is missing");
        }
        if (car.getLicenseNumber() == null || car.getLicenseNumber().isEmpty()) {
            messages.add("License number is missing");
        }
        if (car.getColor() == null || car.getColor().isEmpty()) {
            messages.add("Color is missing");
        }
        if (car.getCoveredKilos() == null || car.getCoveredKilos().isEmpty() || Integer.valueOf(car.getCoveredKilos()).equals(0)) {
            messages.add("Covered kilometers are missing");
        }
        if (car.getIsPremium() == null) {
            messages.add("Premium status is missing");
        }
        if (car.getCreationYear() == null || car.getCreationYear().isEmpty()) {
            messages.add("Creation year is missing");
        }
        if (car.getProperty() == null) {
            messages.add("Property are missing");
        }
        if (car.getModelId() == null) {
            messages.add("Model ID is missing");
        }
        if (car.getChassisNumber() == null || car.getChassisNumber().isEmpty()) {
            messages.add("Chassis number is missing");
        }

        return messages;
    }

    //    @Override
    public ResponseEntity<?> create(String token ,CarDTO carDTO){
        Car carExists = null;
        List<String> emptyFields = checkEmptyFields(carDTO);
        if(!emptyFields.isEmpty()){
            ReturnObject returnObject = new ReturnObject();
            returnObject.setData(null);
            returnObject.setMessage(emptyFields.get(0));
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if(carDTO.getId()!=null){
            Optional<Car> car1 = getById(carDTO.getId());
            if(car1.isPresent()){
                carExists = car1.get();
                ReturnObject returnObject = new ReturnObject();
                returnObject.setData(null);
                returnObject.setMessage("Car Already Exists");
                returnObject.setStatus(false);
            }
        }
        Optional<Car> carByChassisNumber = carRepository.findCarByChassisNumber(carDTO.getChassisNumber());
        if(carByChassisNumber.isPresent()){
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("Car Already Exists");
            returnObject.setData(carByChassisNumber.get());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        Optional<Model> modelOptional = modelRepository.findById(carDTO.getModelId());
        if(modelOptional.isPresent()) {
            carDTO.setModelCode(modelOptional.get().getCode());
            carDTO.setModelId(modelOptional.get().getId());
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("Car Must have model number");
            returnObject.setData(carDTO);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        Integer userId = userService.getUserIdFromToken(token);
        if(userId!=null) {
            if (carExists == null) {
                User userAdmin = userRepository.findUserById(userId);
                if(userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                    carDTO.setCreatedBy(ADMIN_TYPE);
                    if(carDTO.getUserId()!=null) {
                        userId = carDTO.getUserId();
                    }else {
                        ReturnObject returnObject = new ReturnObject();
                        returnObject.setMessage("No User Id is found for admin");
                        returnObject.setData(null);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                    }
                }else{
                    carDTO.setIsActivated((byte) 0);
//                    carDTO.setIsPremium((byte) 0);
                    carDTO.setCreatedBy(USER_TYPE);
                }
                if(carDTO.getIsPremium() == 1){
                    carDTO.setExpirationDate(String.valueOf(LocalDate.now().plusYears(1)));
                }else{
                    carDTO.setIsActivated((byte) 1);
                }
                Car car = new Car(carDTO, userId);
                car.setProperty(carDTO.getProperty());
                car = carRepository.save(car);

                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("New Car Created Successfully");
                returnObject.setData(car);
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            } else {
                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Car Already Exists");
                returnObject.setData(carExists);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("User Not Found");
            returnObject.setData(carDTO);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }
    @Override
    public ResponseEntity update(String token , Integer id, CarDTO dto) {
        Integer userId = userService.getUserIdFromToken(token);
        Car carExists = null;
        Optional<Car> car1 = getById(id);
        if(id!=null){
            if(car1.isPresent()){
                carExists = car1.get();
            }
        }
        if(userId!=null) {
            if (carExists != null) {
                updateCarDetails(carExists , dto , USER_TYPE);
                carExists = carRepository.save(carExists);

                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Updated Car Successfully");
                returnObject.setStatus(true);
                returnObject.setData(carExists);
                return ResponseEntity.ok(returnObject);
            } else {
                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Car Doesn't Exist");
                returnObject.setData(carExists);
                return ResponseEntity.ok(returnObject);
            }
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("User Not Found");
            returnObject.setData(dto);
            return ResponseEntity.ok(returnObject);
        }
    }
    public ResponseEntity updateCarByAdmin(String token , Integer id, CarDTO dto) {
        Integer userId = userService.getUserIdFromToken(token);
        Car carExists = null;
        Optional<Car> car1 = carRepository.findById(id);
        User user = userRepository.getById(userId);
        if (car1.isPresent()) {
            carExists = car1.get();
        }
        if(user.getAccountTypeId() == ADMIN_TYPE) {
            if (carExists != null) {
                updateCarDetails(carExists , dto,ADMIN_TYPE);
                carExists = carRepository.save(carExists);

                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Updated Car Successfully");
                returnObject.setStatus(true);
                returnObject.setData(carExists);
                return ResponseEntity.ok(returnObject);
            } else {
                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Car Doesn't Exist");
                returnObject.setData(carExists);
                return ResponseEntity.ok(returnObject);
            }
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("User Not Authorized");
            returnObject.setData(dto);
            return ResponseEntity.ok(returnObject);
        }
    }

    private void updateCarDetails(Car carExists, CarDTO dto,int userType) {

        /*if(dto.getCreationYear() != null)
        carExists.setCreationYear(dto.getCreationYear());*/
        /*if(dto.getColor() != null)
        carExists.setColor(dto.getColor());*/
        if(dto.getDate() != null)
        carExists.setDate(dto.getDate());
        if(dto.getCoveredKilos() != null)
        carExists.setCoveredKilos(dto.getCoveredKilos());
        if(dto.getDetails() != null)
        carExists.setDetails(dto.getDetails());
/*        if(dto.getChassisNumber() != null)
        carExists.setLicenseNumber(dto.getChassisNumber());*/
/*        if(dto.getPlateNumber() != null)
        carExists.setPlateNumber(dto.getPlateNumber());*/
        if(dto.getNotes() != null)
        carExists.setNotes(dto.getNotes());
        if(dto.getIsPremium() != null && dto.getIsPremium() == 1) {
//            carExists.setExpirationDate(String.valueOf(LocalDate.now().plusYears(1)));
            if(userType == USER_TYPE){
                if(carExists.getIsPremium() == 0 || carExists.getExpirationDate() == null){
                    carExists.setExpirationDate(String.valueOf(LocalDate.now().plusYears(1)));
                    carExists.setIsActivated((byte) 0);
                }
            }
        }else{
            carExists.setExpirationDate(null);
        }
        if(dto.getIsPremium() != null)
            carExists.setIsPremium(dto.getIsPremium());

    }

    @Override
    public ResponseEntity<?> delete(String token,Integer id) {
        Optional<Car> carOptional = carRepository.findById(id);
        Car car = carOptional.get();
        car.setIsDeleted(true);
        ReturnObject returnObject = new ReturnObject();
        try {
            returnObject.setStatus(true);
            returnObject.setMessage("Car Deleted Successfully");
            returnObject.setData(car);
            carRepository.save(car);
            return ResponseEntity.ok(returnObject);
        }catch (Exception exception){
            returnObject.setStatus(true);
            returnObject.setMessage(exception.getMessage());
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }
    public ResponseEntity<?> deleteCarByAdmin(String token,Integer id) {
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.getById(userId);
        ReturnObject returnObject = new ReturnObject();
        if(user.getAccountTypeId() == ADMIN_TYPE) {
            Optional<Car> carOptional = carRepository.findById(id);
            Car car = carOptional.get();
            car.setIsDeleted(true);
            try {
                returnObject.setStatus(true);
                returnObject.setMessage("Car Deleted Successfully");
                returnObject.setData(car);
                carRepository.save(car);
                return ResponseEntity.ok(returnObject);
            } catch (Exception exception) {
                returnObject.setStatus(true);
                returnObject.setMessage(exception.getMessage());
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }else{
            returnObject.setStatus(true);
            returnObject.setMessage("UnAuthorized");
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }
    @Override
    public ResponseEntity<?> getCarsByToken(String token) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = userService.getUserIdFromToken(token);
        if(userId != null) {
            List<CarDTO> usersListOfCars = carRepository.findAllCarsWithModelInfoByUserId(userId);
            if(usersListOfCars.isEmpty()){
                returnObject.setMessage("No Cars");
                returnObject.setStatus(false);
                returnObject.setData(new ArrayList<>());
            }else {
                returnObject.setMessage("Cars Loaded Successfully");
                returnObject.setData(usersListOfCars);
                returnObject.setStatus(true);
            }
        }else{
            returnObject.setMessage("User Is Not Found");
            returnObject.setData(new ArrayList<>());
            returnObject.setStatus(false);
        }
        return ResponseEntity.ok(returnObject);
    }
    public ResponseEntity<?> getCarsForAdminByToken(String token, int page, int size,
                                                    String search, Integer filterUserId) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.getById(userId);
        if (user.getAccountTypeId() == ADMIN_TYPE) {
            String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
            Page<CarDTO> carPage = carRepository.findAllCarsWithFilters(filterUserId, searchParam, PageRequest.of(page, size));
            if (carPage.isEmpty()) {
                returnObject.setMessage("No Cars Found");
                returnObject.setStatus(false);
                returnObject.setData(new ArrayList<>());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            } else {
                List<CarDTO> cars = carPage.getContent();
                cars.forEach(car -> {
                    User owner = userRepository.findUserById(car.getUserId());
                    if (owner != null) car.setUserName(owner.getName());
                });
                returnObject.setTotalPages(carPage.getTotalPages());
                returnObject.setTotalCount(carPage.getTotalElements());
                returnObject.setMessage("Cars Loaded Successfully");
                returnObject.setData(cars);
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            }
        } else {
            returnObject.setMessage("This user isn't Authorized");
            returnObject.setData(new ArrayList<>());
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity<?> getCarsForAdminByTokenAndUserId(String token,Integer userId) {
        ReturnObject returnObject = new ReturnObject();
        Integer adminId = userService.getUserIdFromToken(token);
        User user = userRepository.getById(adminId);
        if(user.getAccountTypeId() == ADMIN_TYPE) {
            List<CarDTO> listOfCars = carRepository.findAllCarsWithModelInfoByUserId(userId);
            if(listOfCars.isEmpty()){
                returnObject.setMessage("No Cars Found");
                returnObject.setStatus(false);
                returnObject.setData(new ArrayList<>());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }else {
                returnObject.setMessage("Cars Loaded Successfully");
                returnObject.setData(listOfCars);
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            }
        }else{
            returnObject.setMessage("This user isn't Authorized");
            returnObject.setData(new ArrayList<>());
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity<?> approveCarByAdmin(String token,Integer carId) {
        ReturnObject returnObject = new ReturnObject();
        Integer adminId = userService.getUserIdFromToken(token);
        User user = userRepository.getById(adminId);
        if(user.getAccountTypeId() == ADMIN_TYPE) {
            Optional<Car> carOptional = carRepository.findById(carId);
            if(carOptional.isEmpty()){
                returnObject.setMessage("No Car Found");
                returnObject.setStatus(false);
                returnObject.setData(new ArrayList<>());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }else {
                Car car = carOptional.get();
                car.setIsActivated((byte) 1);
                carRepository.save(car);
                returnObject.setMessage("Cars Approved Successfully");
                returnObject.setData(car);
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            }
        }else{
            returnObject.setMessage("This user isn't Authorized to Approve car");
            returnObject.setData(carId);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity<?> addCarByAdmin(String token ,Integer userId,CarDTO carDTO){
        Car carExists = null;
        if(userRepository.findById(userId).isEmpty()){
            ReturnObject returnObject = new ReturnObject();
            returnObject.setData(null);
            returnObject.setMessage("No User Found with ID " + userId);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if(carDTO.getId()!=null){
            Optional<Car> car1 = getById(carDTO.getId());
            if(car1.isPresent()){
                carExists = car1.get();
                ReturnObject returnObject = new ReturnObject();
                returnObject.setData(null);
                returnObject.setMessage("Car Already Exists");
                returnObject.setStatus(false);
            }
        }
        Optional<Car> carByChassisNumber = carRepository.findCarByChassisNumber(carDTO.getChassisNumber());
        if(carByChassisNumber.isPresent()){
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("Car Already Exists");
            returnObject.setData(carByChassisNumber.get());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        Optional<Model> modelOptional = modelRepository.findById(carDTO.getModelId());
        if(modelOptional.isPresent()) {
            carDTO.setModelCode(modelOptional.get().getCode());
            carDTO.setModelId(modelOptional.get().getId());
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("Car Must have model number");
            returnObject.setData(carDTO);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        Integer userAdminId = userService.getUserIdFromToken(token);
        if(userAdminId!=null) {
            if (carExists == null) {
                User userAdmin = userRepository.findUserById(userAdminId);
                if(userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                    carDTO.setCreatedBy(ADMIN_TYPE);
                    userAdminId = carDTO.getUserId();
                }else{
                    ReturnObject returnObject = new ReturnObject();
                    returnObject.setStatus(false);
                    returnObject.setMessage("No Authorized");
                    returnObject.setData(null);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
                if(carDTO.getIsPremium() == 1){
                    carDTO.setExpirationDate(String.valueOf(LocalDate.now().plusYears(1)));
                }
                Car car = new Car(carDTO, userId);
                car.setIsActivated((byte) 1);
                car = carRepository.save(car);

                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("New Car Created Successfully");
                returnObject.setData(car);
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            } else {
                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Car Already Exists");
                returnObject.setData(carExists);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("User Not Found");
            returnObject.setData(carDTO);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

}
