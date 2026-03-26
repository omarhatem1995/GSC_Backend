package com.gsc.gsc.admin.controller;

import com.gsc.gsc.admin.dto.ActivateCarDTO;
import com.gsc.gsc.admin.dto.AdminPermissionDTO;
import com.gsc.gsc.admin.dto.CreateAdminDTO;
import com.gsc.gsc.admin.dto.NotificationDTO;
import com.gsc.gsc.admin.service.serviceImplementation.AdminPermissionService;
import com.gsc.gsc.admin.service.serviceImplementation.AdminService;
import com.gsc.gsc.product.service.serviceImplementation.ProductService;
import com.gsc.gsc.bill.dto.AddBillDTO;
import com.gsc.gsc.bill.dto.UpdateBillStatusDTO;
import com.gsc.gsc.bill.service.serviceImplementation.BillService;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.car.service.serviceImplementation.CarService;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.job_cards.dto.JobCardsDTO;
import com.gsc.gsc.job_cards.service.serviceImplementation.JobCardService;
import com.gsc.gsc.user.dto.LoginDTO;
import com.gsc.gsc.user.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;
    @Autowired
    AdminPermissionService adminPermissionService;
    @Autowired
    JobCardService jobCardService;
    @Autowired
    CarService carService;
    @Autowired
    BillService billService;
    @Autowired
    ProductService productService;
    @Autowired
    JwtUtil jwtUtil;


    @PostMapping({"login"})
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginRequestBody
            , HttpServletResponse httpRes) {
        return adminService.adminLogin(loginRequestBody,httpRes);
    }

    @PostMapping("create")
    public ResponseEntity<?> createAdmin(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateAdminDTO dto) {
        return adminService.createAdmin(token, dto);
    }

    @PostMapping("activate_car")
    public ResponseEntity updateCarStats(@RequestHeader("Authorization") String token, @RequestBody ActivateCarDTO activateCarDTO) {
        return adminService.activateCar(token, activateCarDTO);
    }

    @GetMapping("all_job_cards_without_paging")
    private ResponseEntity getJobCardsForAdmin(
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "", required = false) String searchQuery,
            @RequestParam(required = false) Integer carId) {

        return jobCardService.getJobCardsForAdmin(
                token,
                getLangId(lang),
                page,
                size,
                searchQuery,
                carId
        );
    }
    @GetMapping("/all_users")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Integer accountType,   // ADMIN or CUSTOMER
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getAllUsersForAdmin(token, name, phoneNumber, accountType, page, size);
    }

    @PutMapping("activate/{userId}")
    private ResponseEntity<?> activateUser(@RequestHeader("Authorization") String token, @PathVariable Integer userId){
        return adminService.activateUserById(token, userId);
    }

    @GetMapping("user/{userId}")
    private ResponseEntity getUserById(@RequestHeader("Authorization") String token, @PathVariable Integer userId) {
        return adminService.getUserById(token, userId);
    }

    @GetMapping("all_cars")
    private ResponseEntity getAllCars(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminService.getAllCarsForAdmin(token, search, userId, page, size);
    }

    @GetMapping("all_cars_by_user_id")
    private ResponseEntity getAllCarsByUserIdForAdmin(@RequestHeader("Authorization") String token, @RequestParam("userId") Integer userId) {
        return adminService.getAllCarsByUserIdForAdmin(token, userId);
    }

    @PostMapping(value = "/job_card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReturnObject> addJobCardByAdmin(
            @RequestHeader("Authorization") String token,
            @RequestPart("jobCard") JobCardsDTO jobCardsDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {

        ReturnObject returnObject = jobCardService.createByAdmin(token, jobCardsDTO, images);

        return ResponseEntity.status(returnObject.isStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN)
                .body(returnObject);
    }

    @PostMapping(value ="/job_card/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReturnObject> updateJobCardByAdmin(@RequestHeader("Authorization") String token,
                                                             @RequestPart("jobCard") JobCardsDTO jobCardsDTO,
                                                             @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ReturnObject returnObject = jobCardService.updateByAdmin(token, jobCardsDTO, jobCardsDTO.getJobCardId(), images);
        return ResponseEntity.status(returnObject.isStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(returnObject);
    }

    @PostMapping(value ="/job_card/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnObject> updateJobCardByAdminJson(@RequestHeader("Authorization") String token,
                                                                 @RequestBody JobCardsDTO jobCardsDTO) {
        ReturnObject returnObject = jobCardService.updateByAdmin(token, jobCardsDTO, jobCardsDTO.getJobCardId(), null);
        return ResponseEntity.status(returnObject.isStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(returnObject);
    }

    @PutMapping("/job_card/submit/{jobCardId}")
    public ResponseEntity<ReturnObject> submitJobCardByAdmin(@RequestHeader("Authorization") String token,
                                                             @PathVariable Integer jobCardId) {
        ReturnObject returnObject = jobCardService.submitByAdmin(token, jobCardId);
        return ResponseEntity.status(returnObject.isStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(returnObject);
    }

    @PostMapping(value = "bill", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addBillByAdmin(@RequestHeader("Authorization") String token,
                                         @RequestParam(required = false) String macAddress,
                                         @RequestParam(required = false) String mobileVersion,
                                         @RequestBody AddBillDTO billDTO) {
        return billService.createProductBillByAdmin(token, macAddress, mobileVersion, billDTO);
    }

    @PutMapping(value = "bill/{billId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateBillByAdmin(@RequestHeader("Authorization") String token,
                                            @RequestBody AddBillDTO billDTO,
                                            @PathVariable Integer billId) {
        return billService.updateBillByAdmin(token, billDTO, billId);
    }

    @PutMapping(value = "billStatus/{billId}")
    public ResponseEntity updateBillStatus(@RequestHeader("Authorization") String token,
                                           @PathVariable Integer billId,
                                           @RequestBody UpdateBillStatusDTO updateBillStatusDTO) {
        return billService.updateBillStatus(token, billId,updateBillStatusDTO);
    }

    @PostMapping("send_fcm")
    public ResponseEntity sendNotificationToUser(@RequestHeader("Authorization") String token, @RequestBody NotificationDTO notificationDTO) {
        return adminService.sendNotificationByAdmin(token, notificationDTO);
    }

    @GetMapping("all_bills")
    public ResponseEntity getAllUsersBillByToken(
            @RequestHeader(value = "Accept-Language", required = false) String langId,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        return billService.getBillsForAdminByToken(token, userId, search, getLangId(langId), pageable);
    }

    @DeleteMapping("car/{id}")
    public ResponseEntity deleteCar(@RequestHeader("Authorization") String token,
                                    @PathVariable Integer id) {
        return carService.deleteCarByAdmin(token, id);
    }

    @PostMapping("addCar/{userId}")
    public ResponseEntity<?> addCarByAdmin(@RequestHeader("Authorization") String token,
                                           @PathVariable Integer userId,
                                           @RequestBody CarDTO carDTO) {
        return carService.addCarByAdmin(token, userId, carDTO);
    }

    // ─── Admin Permission Management (super admin only) ───────────────────────

    @PutMapping("permissions/{adminId}")
    public ResponseEntity<?> setAdminPermissions(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer adminId,
            @RequestBody AdminPermissionDTO dto) {
        return adminPermissionService.setPermissions(token, adminId, dto);
    }

    @GetMapping("permissions/{adminId}")
    public ResponseEntity<?> getAdminPermissions(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer adminId) {
        return adminPermissionService.getPermissions(token, adminId);
    }

    @PostMapping("permissions/{adminId}/resetPoints/{userId}")
    public ResponseEntity<?> resetAdminPointsLimit(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer adminId,
            @PathVariable Integer userId) {
        return adminPermissionService.resetPointsLimit(token, adminId, userId);
    }

    @GetMapping("product/v3/{productId}")
    public ResponseEntity<?> getProductByIdForAdmin(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer productId) {
        return productService.getProductByIdForAdmin(token, productId);
    }

}