package com.gsc.gsc.point.service;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.model.Point;
import com.gsc.gsc.model.User;
import com.gsc.gsc.point.dto.AddPointsDTO;
import com.gsc.gsc.repo.PointRepository;
import com.gsc.gsc.repo.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.utilities.LanguageConstants.ENGLISH;

@Service
public class PointService {
    @Autowired
    PointRepository pointRepository;

    @Autowired
    UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    public ResponseEntity addPointsToUserFromAdmin(String token ,Integer langId, Integer userId, AddPointsDTO addPointsDTO) {
        Integer adminId = getUserIdFromToken(token);
        User adminUser = userRepository.findUserById(adminId);
        if(adminUser.getAccountTypeId() == ADMIN_TYPE){
        User user = userRepository.findUserById(userId);
        if (user != null) {
            addPointsDTO.setUserId(userId);
            Point point = new Point(addPointsDTO,adminId);

            pointRepository.save(point);

            ReturnObject returnObject = new ReturnObject();
            if (langId == ENGLISH)
                returnObject.setMessage("Added " + addPointsDTO.getPoints() + "Points Successfully ");
            else
                returnObject.setMessage("تم اضافة " + addPointsDTO.getPoints() + " نقطة بنجاح");
            returnObject.setStatus(true);
            returnObject.setData(addPointsDTO);
            return ResponseEntity.ok(returnObject);
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("User Id Not found");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        }else{
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("User UnAuthorized");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);

        }
    }

    public ResponseEntity deletePointsByAdmin(String token,Integer langId,Integer pointsId){
        Integer adminId = getUserIdFromToken(token);
        ReturnObject returnObject = new ReturnObject();
        User adminUser = userRepository.findUserById(adminId);
        if(adminUser.getAccountTypeId() == ADMIN_TYPE) {
            try {
                // Check if the point with the given ID exists
                if (pointRepository.existsById(pointsId)) {
                    // Delete the point with the given ID
                    pointRepository.deleteById(pointsId);
                    returnObject.setMessage("Point deleted successfully");
                    returnObject.setStatus(true);
                    returnObject.setData(pointsId);
                    return ResponseEntity.ok(returnObject);
                } else {
                    returnObject.setMessage("Point not found");
                    returnObject.setStatus(false);
                    returnObject.setData(pointsId);
                    return ResponseEntity.badRequest().body(returnObject);
                }
            } catch (Exception e) {
                // Handle exceptions, log or return an error response
                returnObject.setMessage("Failed to delete Points" + pointsId);
                returnObject.setData(null);
                returnObject.setStatus(false);
                return ResponseEntity.status(500).body(returnObject);
            }
        }else{
            returnObject.setMessage("User UnAuthenticated");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);

        }
    }

    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("getProperty " + token+" ,  "+ userIdString);
        return Integer.parseInt(userIdString);
    }
}
