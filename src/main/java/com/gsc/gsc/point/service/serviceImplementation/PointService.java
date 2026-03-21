package com.gsc.gsc.point.service.serviceImplementation;

import com.gsc.gsc.constants.NotificationTypes;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.model.Notification;
import com.gsc.gsc.model.Point;
import com.gsc.gsc.model.User;
import com.gsc.gsc.point.dto.AddPointsDTO;
import com.gsc.gsc.admin.service.serviceImplementation.AdminPermissionService;
import com.gsc.gsc.repo.NotificationRepository;
import com.gsc.gsc.repo.PointRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.utilities.FirebaseMessagingService;
import com.gsc.gsc.utilities.NotificationMessage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.utilities.LanguageConstants.ENGLISH;

@Service
public class PointService {

    private static final Logger log = LoggerFactory.getLogger(PointService.class);
    @Autowired
    PointRepository pointRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;
    @Autowired
    AdminPermissionService adminPermissionService;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    public ResponseEntity addPointsToUserFromAdmin(String token ,Integer langId, Integer userId, AddPointsDTO addPointsDTO) {
        Integer adminId = getUserIdFromToken(token);
        User adminUser = userRepository.findUserById(adminId);
        if(adminUser.getAccountTypeId() == ADMIN_TYPE){

        // Permission check
        String permissionError = adminPermissionService.checkPointsLimit(adminId, userId, addPointsDTO.getPoints());
        if (permissionError != null) {
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage(permissionError);
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        User user = userRepository.findUserById(userId);
        if (user != null) {
            // Guard: prevent total points from going negative
            if (addPointsDTO.getPoints() < 0) {
                Integer currentTotal = pointRepository.sumPointsByUserId(userId);
                if (currentTotal + addPointsDTO.getPoints() < 0) {
                    ReturnObject returnObject = new ReturnObject();
                    returnObject.setMessage("Insufficient points. User only has " + currentTotal + " points.");
                    returnObject.setStatus(false);
                    returnObject.setData(null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                }
            }

            addPointsDTO.setUserId(userId);
            Point point = new Point(addPointsDTO,adminId);

            pointRepository.save(point);

            notifyCustomerForPoints(user, adminUser.getName(), addPointsDTO.getPoints(), addPointsDTO.getReason());

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

    private void notifyCustomerForPoints(User customer, String adminName, Integer points, String reason) {
        if (customer.getFirebaseToken() == null) return;

        String title = "Points Added by " + adminName;
        String body  = adminName + " has added " + points + " points to your account.";
        if (reason != null && !reason.trim().isEmpty()) {
            body += " Reason: " + reason;
        }

        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setTitle(title);
        notificationMessage.setBody(body);
        notificationMessage.setData(Map.of("message", body));
        notificationMessage.setRecToken(customer.getFirebaseToken());
        String result = firebaseMessagingService.sendNotification(notificationMessage);

        Notification notification = new Notification();
        notification.setUserId(customer.getId());
        notification.setTitle(title);
        notification.setText(body);
        notification.setReason(reason);
        notification.setIsSent(!"Failed".equals(result));
        notification.setNotificationType(NotificationTypes.POINTS);
        notificationRepository.save(notification);
    }

    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        log.info("[TOKEN] Resolved userId={}", userIdString);
        return Integer.parseInt(userIdString);
    }
}
