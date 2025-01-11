package com.gsc.gsc.seller_brand;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.model.SellerBrand;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.SellerBrandRepository;
import com.gsc.gsc.repo.StoreDetailsRepository;
import com.gsc.gsc.repo.StoreRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;

@Service
public class SellerBrandService {
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private SellerBrandRepository sellerBrandRepository;
    @Autowired
    private StoreDetailsRepository storeDetailsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("getProperty " + token + " ,  " + userIdString);
        return Integer.parseInt(userIdString);
    }
    public ResponseEntity<?> createSellerBrand(String token ,SellerBrandDTO sellerBrandDTO) {
        ReturnObject returnObject = new ReturnObject();
        if(token != null) {
            Integer userId = getUserIdFromToken(token);
            User userAdmin = userRepository.findUserById(userId);
            if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                try {
                    SellerBrand sellerBrand = new SellerBrand(sellerBrandDTO);
                    sellerBrand = sellerBrandRepository.save(sellerBrand);
                    returnObject.setStatus(true);
                    returnObject.setData(sellerBrand);
                    returnObject.setMessage("Created Successfully");
                    returnObject.setId(sellerBrand.getId());
                    return ResponseEntity.ok(returnObject);
                } catch (Exception exception) {
                    returnObject.setStatus(false);
                    returnObject.setMessage(exception.getMessage());
                    returnObject.setData(sellerBrandDTO);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                }
            }else{
                returnObject.setMessage("UnAuthorized user");
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnObject);
            }
        }else{
            returnObject.setMessage("No Token Found");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnObject);
        }
    }
}
