package com.gsc.gsc.user.security;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.UserTypes;
import com.gsc.gsc.email.EmailService;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.PointRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.dto.DefaultResponseDTO;
import com.gsc.gsc.user.dto.LoginDTO;
import com.gsc.gsc.user.dto.LoginResponseDTO;
import com.gsc.gsc.user.dto.PointsDTO;
import com.gsc.gsc.user.security.util.JwtUtil;
import com.gsc.gsc.user.security.util.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.constants.UserTypes.NOT_VERIFIED_USER;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PointRepository pointRepository;
    @Autowired
    EmailService emailService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Value("${jwt.expiry.millisecond}")
    private long tokenExpiryTime;

    @Override
    public MyUserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(mail);
        MyUserDetails userDetails = new MyUserDetails(user);
        return userDetails;
    }

    public String generateToken(String mail , HttpServletResponse httpRes){
        final MyUserDetails userDetails = this.loadUserByUsername(mail);
        final String jwt = jwtTokenUtil.generateToken(userDetails.getUserId(), UserTypes.USER_TYPE
                , tokenExpiryTime);
        addCookie(jwt , httpRes, tokenExpiryTime);
        return jwt;
    }
    public ResponseEntity<?> login(String phone, String password,
                                   HttpServletResponse httpRes) {
        try {
            // Check if the user exists
            User user = userRepository.findByPhone(phone);
            if (user == null) {
                // User does not exist
                return new ResponseEntity<>(new DefaultResponseDTO(3, "User does not exist, please register"), HttpStatus.NOT_FOUND);
            }

            if (user.getIsActive() != null && user.getIsActive() == 0) {
                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Your account is inactive. You cannot login.");
                returnObject.setData(null);
                return new ResponseEntity<>(returnObject, HttpStatus.FORBIDDEN);
            }

            // Attempt authentication (password verification)
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    phone, password));
        } catch (UsernameNotFoundException e) {
            // User does not exist
            return new ResponseEntity<>(new DefaultResponseDTO(3, "User does not exist, please register"), HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            // Wrong password
            return new ResponseEntity<>(new DefaultResponseDTO(1, "Invalid password"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            // General error or wrong email (this may vary based on implementation)
            return new ResponseEntity<>(new DefaultResponseDTO(2, "Invalid phone"), HttpStatus.FORBIDDEN);
        }

        final MyUserDetails userDetails = this.loadUserByUsername(phone);
        final User user = userRepository.findByPhone(phone);
        LoginResponseDTO loginResponseDTO = userRepository.getLoginDTOByUserId(userDetails.getUserId());

        final String jwt = jwtTokenUtil.generateToken(userDetails.getUserId(), UserTypes.USER_TYPE
                , tokenExpiryTime);
        addCookie(jwt , httpRes, tokenExpiryTime);

        loginResponseDTO.setToken(jwt);
        List<PointsDTO> pointsList =  pointRepository.findPointsByUserId(userDetails.getUserId());
        Integer userTotalPoints = 0;
        for(int i = 0; i<pointsList.size(); i++){
            userTotalPoints += pointsList.get(i).getPointsNumber();
        }
        loginResponseDTO.setPoints(userTotalPoints);

        if(loginResponseDTO.getIsVerified()!=null && loginResponseDTO.getIsVerified()) {
            loginResponseDTO.setCookieExpiry(LocalDateTime.now().plusSeconds(tokenExpiryTime));
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("Logged In Successfully");
            returnObject.setData(loginResponseDTO);
            return new ResponseEntity<>(returnObject, HttpStatus.OK);
        }else{
            String otp = generateOtp();
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("User Not Verified");
            returnObject.setData(null);
            returnObject.setId(NOT_VERIFIED_USER);
            emailService.sendOtpEmail(loginResponseDTO.getMail(),otp);
            user.setVerificationOTP(otp);
            userRepository.save(user);
            return new ResponseEntity<>(returnObject, HttpStatus.FORBIDDEN);
        }
    }
    private String generateOtp() {
        // Generate a 6-digit OTP
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
    protected void addCookie(String token,
                             HttpServletResponse httpRes,
                             long tokenExpiry) {

        Cookie cookie = new Cookie("Authorization", token);
        cookie.setHttpOnly(true);

        int expiry = (int) (tokenExpiry / 1000) ;
        cookie.setMaxAge(expiry);
        cookie.setPath("/");
        cookie.setSecure(true);

        httpRes.addCookie(cookie);
    }

    public void logout(HttpServletResponse httpRes) {
        addCookie("",  httpRes, 0);
    }


}