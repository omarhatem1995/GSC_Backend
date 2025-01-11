package com.gsc.gsc.user.service.servicesImplementation;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.email.EmailService;
import com.gsc.gsc.model.FirebaseToken;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.PointRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.dto.*;
import com.gsc.gsc.user.security.AuthenticationService;
import com.gsc.gsc.user.security.util.JwtUtil;
import com.gsc.gsc.user.service.servicesInterface.IUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gsc.gsc.constants.UserTypes.*;

@Service
public class UserService implements IUserService {

    @Autowired
    private JwtUtil jwtUtil;
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PointRepository pointRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Value("${jwt.expiry.millisecond}")
    private long tokenExpiryTime;
    @Override
    public User getById(Integer id) {
        return userRepository.findUserById(id);
    }

    String emailRegex = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    Pattern pattern = Pattern.compile(emailRegex);
    Matcher matcher ;
    public ResponseEntity getUserByToken(String token) {
        Integer userIdFromToken =  getUserIdFromToken(token);
        ReturnObject returnObject = new ReturnObject();
        returnObject.setMessage("Success");
        returnObject.setStatus(true);
        returnObject.setData(userRepository.findUserById(userIdFromToken));
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> findPointsByToken(String token){
        Integer userId = getUserIdFromToken(token);
        ReturnObject returnObject = new ReturnObject();
        if(userId != null){
            returnObject.setMessage("Successfully");
            GetPointsDTO getPointsDTO = new GetPointsDTO();
            List<PointsDTO> pointsDTO = pointRepository.findPointsByUserId(userId);
            Integer total = 0;
            getPointsDTO.setPointsDTO(pointsDTO);
            for(int i=0;i<pointsDTO.size();i++){
                total += pointsDTO.get(i).getPointsNumber();
            }
            getPointsDTO.setTotalPoints(total);
            returnObject.setData(getPointsDTO);
            returnObject.setStatus(true);
            return ResponseEntity.ok(returnObject);
        }else{
            returnObject.setMessage("Failed");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);

        }
    }

    //Using Gmail
    public ResponseEntity verifyUser(UserDTO userDTO){
        ReturnObject returnObject = new ReturnObject();
        if(userDTO.getPhone()==null){
            returnObject.setMessage("Phone is Mandatory");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        userDTO.setPhone(cleanMobileNumber(userDTO.getPhone()));
        Optional<User> userOptional = userRepository.findOptionalByPhone(userDTO.getPhone());
        if(userDTO.getMail()==null){
            returnObject.setMessage("Mail is Mandatory");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if(userOptional.isPresent()) {
            User user = userOptional.get();
            if(user.getVerificationOTP().trim().equals(userDTO.getVerificationOTP().trim())) {
                user.setIsVerified(true);
                userRepository.save(user);
                returnObject.setData(user);
                returnObject.setStatus(true);
                returnObject.setMessage("User Verified Successfully");
                return ResponseEntity.status(HttpStatus.OK).body(returnObject);
            }else{
                returnObject.setData(null);
                returnObject.setStatus(false);
                returnObject.setMessage("Verification OTP is incorrect");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }else{
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("User Doesn't exist");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }
    //Vonage
    public void verifyUser(String phone){
        User user = userRepository.findByPhone(phone);
        user.setIsVerified(true);
        userRepository.save(user);
    }
    private String generateOtp() {
        // Generate a 6-digit OTP
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
    public static String cleanMobileNumber(String phoneNumber) {
        if (phoneNumber.startsWith("+2")) {
            return phoneNumber.substring(2);
        }
        return phoneNumber.trim();
    }

    public ResponseEntity<?> resendOtp(UserDTO userDTO){
        ReturnObject returnObject = new ReturnObject();
        if(userDTO.getPhone() == null){
                returnObject.setStatus(false);
                returnObject.setMessage("Phone can't be empty");
                returnObject.setData(userDTO);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }else{
            userDTO.setPhone(cleanMobileNumber(userDTO.getPhone()));
        }
        if(userDTO.getMail() == null){
                returnObject.setStatus(false);
                returnObject.setMessage("Mail can't be empty");
                returnObject.setData(userDTO);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        User userExists = userRepository.findByMailAndPhone(userDTO.getMail(),userDTO.getPhone());
        if(userExists != null) {
            User user = userExists;
            String otp = generateOtp();
            user.setVerificationOTP(otp);
            emailService.sendOtpEmail(user.getMail(), otp);
            user = userRepository.save(user);
            userDTO.setId(user.getId());
        }
        returnObject.setMessage("code sent successfully");
        returnObject.setData(userDTO);
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> create(UserDTO userDTO , HttpServletResponse httpRes){
        ReturnObject returnObject = new ReturnObject();
        User userExists = new User();

        matcher = pattern.matcher(userDTO.getMail());
        if (!matcher.matches()) {
            returnObject.setStatus(false);
            returnObject.setMessage("Invalid email format");
            returnObject.setData(userDTO);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(userDTO.getCustomerType() == null){
            returnObject.setMessage("Please specify a user type");
            returnObject.setData(userDTO);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(userDTO.getCustomerType() != USER_TYPE && userDTO.getCustomerType() != BUSINESS_TYPE){
            returnObject.setMessage("Please specify a defined user type");
            returnObject.setData(userDTO);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(userDTO.getPhone() != null){
            userExists = userRepository.findByPhone(userDTO.getPhone());
            userDTO.setPhone(cleanMobileNumber(userDTO.getPhone()));
            if(userExists != null){
                    returnObject.setStatus(false);
                    returnObject.setMessage("Phone already exists");
                    returnObject.setData(userDTO);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }
        if(userDTO.getMail() != null){
            userExists = userRepository.findByMail(userDTO.getMail());
            if(userExists != null){
                    returnObject.setStatus(false);
                    returnObject.setMessage("Mail already exists");
                    returnObject.setData(userDTO);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }
        if(userExists==null) {
            User user = new User(userDTO);
            String password = bCryptPasswordEncoder.encode(user.getPassword());
            user.setPassword(password);
            String otp = generateOtp();
            user.setVerificationOTP(otp);
            emailService.sendOtpEmail(user.getMail(),otp);
            user = userRepository.save(user);
            userDTO.setId(user.getId());
            userDTO.setToken(jwtTokenUtil.generateToken(user.getId(),USER_TYPE,tokenExpiryTime));

            returnObject.setMessage("New User Created Successfully");
            returnObject.setData(userDTO);
            return ResponseEntity.ok(returnObject);
        }else{
            returnObject.setMessage("User Already Exists");
            returnObject.setData(userExists);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    @Override
    public ResponseEntity<?> update(String token, UserDTO dto) {
        Integer userId = getUserIdFromToken(token);
        ReturnObject returnObject = new ReturnObject();
        User user = userRepository.getById(userId);
        if(dto.getPhone()!=null){
            dto.setPhone(cleanMobileNumber(dto.getPhone()));
        }
        if(user != null) {
            if (dto.getNewPassword() != null) {
                String newPassword = bCryptPasswordEncoder.encode(dto.getNewPassword());
                user.setPassword(newPassword);
            }
            updateUserFields(user, dto);
            User updatedUser = userRepository.save(user);
            returnObject.setStatus(true);
            returnObject.setMessage("Updated User Successfully");
            returnObject.setData(updatedUser);
            return ResponseEntity.ok(returnObject);
        }else{
            returnObject.setStatus(false);
            returnObject.setMessage("User Not Found");
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity<?> updateByAdmin(String token, UserDTO dto) {
        ReturnObject returnObject = new ReturnObject();
        Integer adminUserId = getUserIdFromToken(token);
        User userAdmin = userRepository.getById(adminUserId);
        if(userAdmin.getAccountTypeId() == ADMIN_TYPE) {
            User user = userRepository.getById(dto.getId());
            if (dto.getNewPassword() != null) {
                String newPassword = bCryptPasswordEncoder.encode(dto.getNewPassword());
                user.setPassword(newPassword);
            }
            updateUserFields(user, dto);
            returnObject.setStatus(true);
            returnObject.setMessage("Updated User Successfully");
            User updatedUser = userRepository.save(user);
            returnObject.setData(updatedUser);
            return ResponseEntity.ok(returnObject);
        }else {
            returnObject.setStatus(false);
            returnObject.setMessage("You are not authorized to update other users");
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }
    private void updateUserFields(User user, UserDTO dto) {
        user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        if(dto.getMail() != null)
        user.setMail(dto.getMail());
        if(dto.getName() != null)
        user.setName(dto.getName());
        if(dto.getPhone() != null)
        user.setPhone(dto.getPhone());
        if(dto.getCommercialLicense() != null)
        user.setCommercialLicense(dto.getCommercialLicense());
        if(dto.getCommercialRegistry() != null)
        user.setCommercialRegistry(dto.getCommercialRegistry());
        if(dto.getEstablishmentRegistration() != null)
        user.setEstablishmentRegistration(dto.getEstablishmentRegistration());
    }

    @Override
    public ResponseEntity<?> delete(Integer id) {
        return null;
    }

    private ResponseEntity<DefaultResponseDTO> getDBIntegrityError(Exception e) {
        if (e.getMessage().contains("mobile_UNIQUE"))
            return new ResponseEntity<DefaultResponseDTO>( new DefaultResponseDTO(1, "Mobile already exist"), HttpStatus.CONFLICT);
        if (e.getMessage().contains("username_UNIQUE"))
            return new ResponseEntity<>(new DefaultResponseDTO(2, "User already exist"), HttpStatus.CONFLICT);
        System.out.println(e.getMessage());
        return new ResponseEntity<>(new DefaultResponseDTO(3, "Bad request"), HttpStatus.BAD_REQUEST);
    }
    public  ResponseEntity<?>  logout(HttpServletResponse httpRes) {
        ReturnObject returnObject = new ReturnObject();
        try {
            authenticationService.logout(httpRes);
            returnObject.setStatus(true);
            returnObject.setData(null);
            returnObject.setMessage("Success");
            return ResponseEntity.ok(returnObject);
        }catch (Exception exception){
            returnObject.setMessage(exception.getMessage());
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    public ResponseEntity<?> updateFirebaseToken(String token , FirebaseToken firebaseToken) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                String newFirebaseToken = firebaseToken.getFirebaseToken();
                User user = userOptional.get();
                user.setFirebaseToken(newFirebaseToken);
                userRepository.save(user);
                returnObject.setStatus(true);
                returnObject.setData(user);
                returnObject.setMessage("Success");
                return ResponseEntity.ok(returnObject);
            }else{
                returnObject.setData(null);
                returnObject.setStatus(false);
                returnObject.setMessage("Failed");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }catch (Exception exception){
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("Failed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }
    public ResponseEntity<?> login(LoginDTO loginRequestBody, HttpServletResponse httpRes) {
        loginRequestBody.setPhone(cleanMobileNumber(loginRequestBody.getPhone()));
        System.out.println("login : " + loginRequestBody.getPhone());
        return authenticationService.login(loginRequestBody.getPhone(),
                loginRequestBody.getPassword(),httpRes);
    }


    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("getProperty " + token+" ,  "+ userIdString);
        return Integer.parseInt(userIdString);
    }

}
