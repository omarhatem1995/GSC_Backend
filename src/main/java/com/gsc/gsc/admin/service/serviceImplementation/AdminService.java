package com.gsc.gsc.admin.service.serviceImplementation;

import com.gsc.gsc.admin.dto.ActivateCarDTO;
import com.gsc.gsc.admin.dto.NotificationDTO;
import com.gsc.gsc.admin.service.serviceInterface.IAdminService;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.car.dto.UsersCarsDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.model.Car;
import com.gsc.gsc.model.Notification;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.security.util.JwtUtil;
import com.gsc.gsc.user.service.servicesImplementation.UserService;
import com.gsc.gsc.utilities.FirebaseMessagingService;
import com.gsc.gsc.utilities.NotificationMessage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;

@Service
public class AdminService implements IAdminService {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountTypeRepository accountTypeRepository;
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private JwtUtil jwtUtil;
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private ModelRepository modelRepository;

    @Override
    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<?> getCars(String token) {
        return null;
    }

    public ResponseEntity getAllUsersForAdmin(String token) {
        ReturnObject returnObject = new ReturnObject();
        try {
            Integer userIdFromToken = getUserIdFromToken(token);
            if (userRepository.findUserById(userIdFromToken).getAccountTypeId() == ADMIN_TYPE) {
                returnObject.setMessage("Success");
                returnObject.setData(userRepository.findAllExceptAdmins());
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            } else {
                returnObject.setMessage("This user is not Authorized");
                returnObject.setStatus(false);
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        } catch (ExpiredJwtException e) {
            // Handle expired token
            System.out.println("JWT has expired: " + e.getMessage());
            returnObject.setMessage("JWT has expired: " + e.getMessage());
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);

            // Handle the expired token case (e.g., by logging, notifying the user, etc.)
        } catch (JwtException e) {
            // Handle other JWT exceptions
            System.out.println("JWT processing error: " + e.getMessage());
            returnObject.setMessage("JWT has expired: " + e.getMessage());
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            // Handle the case where the token is invalid or some other JWT error occurs
        }
    }

    public ResponseEntity getAllCarsForAdmin(String token) {
        Integer userIdFromToken = getUserIdFromToken(token);
        ReturnObject returnObject = new ReturnObject();
        if (userRepository.findUserById(userIdFromToken).getAccountTypeId() == ADMIN_TYPE) {
            List<Car> carsList = carRepository.findAll();
            List<UsersCarsDTO> usersCarsDTOS = new ArrayList<>();
            for (int i = 0; i < carsList.size(); i++) {
                UsersCarsDTO usersCarsDTO;
                if (carsList.get(i).getModelId() != null)
                    usersCarsDTO = new UsersCarsDTO(carsList.get(i), modelRepository.findById(carsList.get(i).getModelId()));
                else
                    usersCarsDTO = new UsersCarsDTO(carsList.get(i));
                usersCarsDTO.setUserName(userRepository.findUserById(carsList.get(i).getUserId()).getName());
                usersCarsDTOS.add(usersCarsDTO);
            }
            returnObject.setMessage("Success");
            returnObject.setData(usersCarsDTOS);
            returnObject.setStatus(true);
        } else {
            returnObject.setMessage("This user is not Authorized");
            returnObject.setStatus(false);
            returnObject.setData(null);
        }
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity getAllCarsByUserIdForAdmin(String token, Integer userId) {
        Integer userIdFromToken = getUserIdFromToken(token);
        ReturnObject returnObject = new ReturnObject();
        if (userRepository.findUserById(userIdFromToken).getAccountTypeId() == ADMIN_TYPE) {
            returnObject.setMessage("Success");
            returnObject.setData(carRepository.findAllByUserId(userId));
            returnObject.setStatus(true);
        } else {
            returnObject.setMessage("This user is not Authorized");
            returnObject.setStatus(false);
            returnObject.setData(null);
        }
        return ResponseEntity.ok(returnObject);
    }

    @Override
    public ResponseEntity update(String token, Integer id, CarDTO dto) {
        return null;
    }

    @Override
    public ResponseEntity activateCar(String token, ActivateCarDTO activateCarDTO) {
        Integer userId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();
        if (userAdmin != null) {
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                activateCar(activateCarDTO.getCarId(), activateCarDTO.getIsActivated());
                Optional<Car> car = carRepository.findById(activateCarDTO.getCarId());
                returnObject.setStatus(true);
                returnObject.setData(car);
                returnObject.setMessage("Status changed Successfully");
                return ResponseEntity.ok(returnObject);
            } else {
//                return ResponseEntity.ok();
                returnObject.setStatus(false);
                returnObject.setData(null);
                returnObject.setMessage("This user is not Authorized to change car status");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }
        } else {
            returnObject.setStatus(false);
            returnObject.setData(null);
            returnObject.setMessage("User Not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity<?> sendNotificationByAdmin(String token, NotificationDTO notificationDTO) {
        Integer userId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();
        if (userId != null) {
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                if (notificationDTO.getUserIds().isEmpty()) {
                    returnObject.setMessage("No Users Selected");
                    returnObject.setData(null);
                    returnObject.setStatus(false);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                } else if (notificationDTO.getUserIds().get(0) != 0) {
                    for (int i = 0; i < notificationDTO.getUserIds().size(); i++) {
                        Optional<User> userOptional = userRepository.findById(notificationDTO.getUserIds().get(i));
                        if (userOptional.isPresent()) {
                            User user = userOptional.get();
                            NotificationMessage notificationMessage = new NotificationMessage();
                            if (user.getFirebaseToken() != null) {
                                notificationMessage.setRecToken(user.getFirebaseToken());
                                notificationMessage.setBody(notificationDTO.getBody());
                                notificationMessage.setData(Map.of("Message : ", notificationDTO.getBody()));
                                notificationMessage.setTitle(notificationDTO.getBody());
                                Notification notification = new Notification();
                                notification.setUserId(user.getId());
                                notification.setText(notificationDTO.getBody());
                                notificationRepository.save(notification);
                                firebaseMessagingService.sendNotification(notificationMessage);
                            }
                        }
                    }
                } else {
                    List<User> usersList = userRepository.findAll();
                    for (int i = 0; i < usersList.size(); i++) {
                        User user = usersList.get(i);
                        NotificationMessage notificationMessage = new NotificationMessage();
                        if (user.getFirebaseToken() != null) {
                            notificationMessage.setRecToken(user.getFirebaseToken());
                            notificationMessage.setBody(notificationDTO.getBody());
                            notificationMessage.setData(Map.of("Message : ", notificationDTO.getBody()));
                            notificationMessage.setTitle(notificationDTO.getBody());
                            Notification notification = new Notification();
                            notification.setUserId(user.getId());
                            notification.setText(notificationDTO.getBody());
                            notificationRepository.save(notification);
                            firebaseMessagingService.sendNotification(notificationMessage);
                        }
                    }
                }
                returnObject.setMessage("Success");
                returnObject.setStatus(true);
                returnObject.setData(null);
                return ResponseEntity.ok(returnObject);
            } else {
                returnObject.setMessage("Not Authorized to send Notification");
                returnObject.setStatus(false);
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }

        } else {
            returnObject.setMessage("Not Authorized to send Notification");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    private void activateCar(Integer carId, Byte isActivated) {
        LocalDateTime time = LocalDateTime.now().plus(1, ChronoUnit.YEARS);
        Date expirationDate = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());

        // Convert the Date to Timestamp
        Timestamp expirationTimestamp = new Timestamp(expirationDate.getTime());

        // Pass the Timestamp to the repository
        carRepository.updateActivationStatus(carId, isActivated, expirationTimestamp);
    }


    @Override
    public ResponseEntity delete(Integer id) {
        return null;
    }

    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("token :  " + token + " , userID : " + userIdString);
        return Integer.parseInt(userIdString);
    }

    public ResponseEntity getUserById(String token, Integer userId) {
        ReturnObject returnObject = new ReturnObject();
        if (token != null) {
            Integer adminId = getUserIdFromToken(token);
            User userAdmin = userRepository.findUserById(adminId);
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                User user = userRepository.findUserById(userId);
                if (user != null) {
                    returnObject.setData(user);
                    returnObject.setStatus(true);
                    returnObject.setMessage("Loaded Successfully");
                    return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                } else {
                    returnObject.setData(null);
                    returnObject.setMessage("No user found");
                    returnObject.setStatus(false);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
            } else {
                returnObject.setData(null);
                returnObject.setMessage("Not Authorized");
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        } else {
            returnObject.setData(null);
            returnObject.setMessage("Not Authorized");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }
}
