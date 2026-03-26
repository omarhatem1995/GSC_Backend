package com.gsc.gsc.admin.service.serviceImplementation;

import com.google.api.Http;
import com.gsc.gsc.admin.dto.ActivateCarDTO;
import com.gsc.gsc.admin.dto.CreateAdminDTO;
import com.gsc.gsc.admin.dto.NotificationDTO;
import com.gsc.gsc.admin.service.serviceInterface.IAdminService;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.car.dto.UsersCarsDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.model.Car;
import com.gsc.gsc.model.Notification;
import com.gsc.gsc.model.Point;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.dto.GetAllUsers;
import com.gsc.gsc.user.dto.LoginDTO;
import com.gsc.gsc.user.security.AuthenticationService;
import com.gsc.gsc.user.security.util.JwtUtil;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import com.gsc.gsc.utilities.FirebaseMessagingService;
import com.gsc.gsc.utilities.NotificationMessage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.gsc.gsc.admin.service.serviceImplementation.AdminPermissionService;
import com.gsc.gsc.repo.AdminPermissionRepository;
import com.gsc.gsc.user.dto.LoginResponseDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static com.gsc.gsc.constants.NotificationTypes.ADMIN;
import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.constants.UserTypes.BUSINESS_TYPE;
import static com.gsc.gsc.constants.UserTypes.USER_TYPE;
import static com.gsc.gsc.user.service.serviceImplementation.UserService.cleanMobileNumber;

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
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private AdminPermissionService adminPermissionService;
    @Autowired
    private AdminPermissionRepository adminPermissionRepository;

    @Autowired
    private AuthenticationService authenticationService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<?> adminLogin(LoginDTO loginDTO, HttpServletResponse httpRes) {

        ReturnObject returnObject = new ReturnObject();

        User user = userRepository.findByPhone(loginDTO.getPhone());

        if (user == null) {
            returnObject.setStatus(false);
            returnObject.setMessage("User not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        // Allow login only for admins
        if (!user.getAccountTypeId().equals(ADMIN_TYPE)) {
            returnObject.setStatus(false);
            returnObject.setMessage("Forbidden: Admin access only");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        ResponseEntity<?> loginResponse = authenticationService.login(
                loginDTO.getPhone(),
                loginDTO.getPassword(),
                httpRes
        );

        // If login succeeded, inject the admin's permissions into the response
        if (loginResponse.getStatusCode() == HttpStatus.OK && loginResponse.getBody() instanceof ReturnObject) {
            ReturnObject body = (ReturnObject) loginResponse.getBody();
            if (body.getData() instanceof LoginResponseDTO) {
                LoginResponseDTO loginResponseDTO = (LoginResponseDTO) body.getData();
                adminPermissionRepository.findByAdminId(loginResponseDTO.getId())
                        .ifPresent(loginResponseDTO::setPermissions);
            }
        }

        return loginResponse;
    }
    @Override
    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<?> getCars(String token) {
        return null;
    }


    public ResponseEntity<ReturnObjectPaging> getAllUsersForAdmin(
            String token,
            String name,
            String phone,
            Integer accountTypeId, // ADMIN_TYPE or CUSTOMER_TYPE
            int page,
            int size
    ) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();
        try {
            Integer userIdFromToken = getUserIdFromToken(token);
            User currentUser = userRepository.findById(userIdFromToken).orElse(null);

            if (currentUser == null || !currentUser.getAccountTypeId().equals(ADMIN_TYPE)) {
                returnObject.setMessage("This user is not authorized");
                returnObject.setStatus(false);
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Page<User> users = userRepository.findAllWithFilters(accountTypeId, name, phone, pageable);

            List<GetAllUsers> result = users.getContent().stream().map(user -> {

                GetAllUsers dto = new GetAllUsers();

                dto.setId(user.getId());
                dto.setName(user.getName());
                dto.setAccountTypeId(user.getAccountTypeId());
                dto.setMail(user.getMail());
                dto.setCommercialRegistry(user.getCommercialRegistry());
                dto.setCommercialLicense(user.getCommercialLicense());
                dto.setEstablishmentRegistration(user.getEstablishmentRegistration());
                dto.setTaxCard(user.getTaxCard());
                dto.setMailbox(user.getMailbox());
                dto.setAddress(user.getAddress());
                dto.setPhone(user.getPhone());
                dto.setIsActive(user.getIsActive());

                // calculate points
                List<Point> points = pointRepository.findAllByUserId(user.getId());

                long totalPoints = 0;

                for (Point p : points) {
                    if (p.getOperationType() == 1) {
                        totalPoints += p.getPointsNumber();
                    } else if (p.getOperationType() == 2) {
                        totalPoints -= p.getPointsNumber();
                    }
                }

                dto.setPoints(totalPoints);

                return dto;

            }).collect(Collectors.toList());

            returnObject.setMessage("Success");
            returnObject.setStatus(true);
            returnObject.setData(result);
            returnObject.setTotalCount(users.getTotalElements());
            returnObject.setTotalPages(users.getTotalPages());
            return ResponseEntity.ok(returnObject);

        } catch (ExpiredJwtException e) {
            returnObject.setMessage("JWT has expired: " + e.getMessage());
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);

        } catch (JwtException e) {
            returnObject.setMessage("JWT processing error: " + e.getMessage());
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }


    public ResponseEntity getAllCarsForAdmin(String token, String search, Integer filterUserId, int page, int size) {
        Integer userIdFromToken = getUserIdFromToken(token);
        ReturnObjectPaging returnObject = new ReturnObjectPaging();
        if (userRepository.findUserById(userIdFromToken).getAccountTypeId() == ADMIN_TYPE) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            // Pass null for empty search string so the JPQL IS NULL check works correctly
            String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
            Page<CarDTO> carsPage = carRepository.findAllCarsWithFilters(filterUserId, searchParam, pageable);
            List<CarDTO> carsList = carsPage.getContent();
            // Attach user name to each result
            carsList.forEach(car -> {
                User owner = userRepository.findUserById(car.getUserId());
                if (owner != null) car.setUserName(owner.getName());
            });
            returnObject.setMessage("Success");
            returnObject.setData(carsList);
            returnObject.setStatus(true);
            returnObject.setTotalCount(carsPage.getTotalElements());
            returnObject.setTotalPages(carsPage.getTotalPages());
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
        ReturnObject returnObject = new ReturnObject();

        if (userId == null) {
            returnObject.setMessage("Not Authorized to send Notification");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        User userAdmin = userRepository.findUserById(userId);
        if (userAdmin.getAccountTypeId() != ADMIN_TYPE) {
            returnObject.setMessage("Not Authorized to send Notification");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if (!adminPermissionService.canSendNotifications(userId)) {
            returnObject.setMessage("You do not have permission to send notifications");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        // Build target user list based on targetType
        List<User> targetUsers = new ArrayList<>();
        String targetType = notificationDTO.getTargetType();

        if ("ALL".equals(targetType)) {
            // All non-admin users
            targetUsers = userRepository.findAll().stream()
                    .filter(u -> u.getAccountTypeId() != ADMIN_TYPE)
                    .collect(Collectors.toList());
        } else if ("BUSINESS".equals(targetType)) {
            // Only business customers
            targetUsers = userRepository.findAll().stream()
                    .filter(u -> u.getAccountTypeId() != null && u.getAccountTypeId() == BUSINESS_TYPE)
                    .collect(Collectors.toList());
        } else if ("PERSONAL".equals(targetType)) {
            // Only personal customers
            targetUsers = userRepository.findAll().stream()
                    .filter(u -> u.getAccountTypeId() != null && u.getAccountTypeId() == USER_TYPE)
                    .collect(Collectors.toList());
        } else {
            // No targetType — fall back to explicit user ID list
            if (notificationDTO.getUserIds() == null || notificationDTO.getUserIds().isEmpty()) {
                returnObject.setMessage("No Users Selected");
                returnObject.setData(null);
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }
            for (Integer targetId : notificationDTO.getUserIds()) {
                userRepository.findById(targetId).ifPresent(targetUsers::add);
            }
        }

        // Send notification to each target user
        for (User user : targetUsers) {
            if (user.getFirebaseToken() != null) {
                NotificationMessage notificationMessage = new NotificationMessage();
                notificationMessage.setRecToken(user.getFirebaseToken());
                notificationMessage.setTitle(notificationDTO.getTitle());
                notificationMessage.setBody(notificationDTO.getBody());
                notificationMessage.setData(Map.of("message", notificationDTO.getBody()));
                String result = firebaseMessagingService.sendNotification(notificationMessage);
                boolean sent = !"Failed".equals(result);
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setTitle(notificationDTO.getTitle());
                notification.setText(notificationDTO.getBody());
                notification.setIsSent(sent);
                notification.setNotificationType(ADMIN);
                notificationRepository.save(notification);
                if (sent) {
                    System.out.println("[Notification] SUCCESS | userId: " + user.getId() + " | name: " + user.getName() + " | msgId: " + result);
                } else {
                    System.err.println("[Notification] FAILED  | userId: " + user.getId() + " | name: " + user.getName());
                }
            }
        }

        returnObject.setMessage("Success");
        returnObject.setStatus(true);
        returnObject.setData(null);
        return ResponseEntity.ok(returnObject);
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
        return userService.getUserIdFromToken(token);
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

    public ResponseEntity<?> activateUserById(String token, Integer userId) {
        ReturnObject returnObject = new ReturnObject();
        if (token != null) {
            Integer adminId = getUserIdFromToken(token);
            User userAdmin = userRepository.findUserById(adminId);
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                User user = userRepository.findUserById(userId);
                if (user == null) {
                    returnObject.setStatus(false);
                    returnObject.setMessage("User not found");
                    returnObject.setData(null);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
                if (user.getIsActive() == 1) {
                    user.setIsActive(0);
                    userRepository.save(user);
                    returnObject.setMessage("DeActivated User Successfully");
                    returnObject.setStatus(true);
                    returnObject.setData(null);
                    return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                }
                user.setIsActive(1);
                userRepository.save(user);
                returnObject.setMessage("Activated User Successfully");
                returnObject.setStatus(true);
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.OK).body(returnObject);
            } else {
                returnObject.setStatus(false);
                returnObject.setMessage("Can't change user status with non admin user");
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }
        returnObject.setStatus(false);
        returnObject.setMessage("Can't change user status with non admin user");
        returnObject.setData(null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
    }

    public ResponseEntity<?> createAdmin(String token, CreateAdminDTO dto) {
        ReturnObject returnObject = new ReturnObject();
        Integer requesterId = userService.getUserIdFromToken(token);

        if (!adminPermissionService.isSuperAdmin(requesterId)) {
            returnObject.setStatus(false);
            returnObject.setMessage("Only super admins can create admin accounts");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            returnObject.setStatus(false);
            returnObject.setMessage("Phone number is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            returnObject.setStatus(false);
            returnObject.setMessage("Password is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        String cleanPhone = cleanMobileNumber(dto.getPhone());

        User existing = userRepository.findByPhone(cleanPhone);
        if (existing != null) {
            returnObject.setStatus(false);
            returnObject.setMessage("A user with this phone number already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(returnObject);
        }

        User admin = new User();
        admin.setName(dto.getName());
        admin.setPhone(cleanPhone);
        admin.setMail(dto.getMail());
        admin.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        admin.setAccountTypeId(ADMIN_TYPE);
        admin.setIsVerified(true);
        admin.setIsActive(1);

        User saved = userRepository.save(admin);

        // Don't return the hashed password
        saved.setPassword(null);

        returnObject.setStatus(true);
        returnObject.setMessage("Admin created successfully");
        returnObject.setData(saved);
        return ResponseEntity.ok(returnObject);
    }

}
