package com.gsc.gsc.admin.controller;

import com.gsc.gsc.admin.dto.ActivateCarDTO;
import com.gsc.gsc.admin.dto.NotificationDTO;
import com.gsc.gsc.admin.service.serviceImplementation.AdminService;
import com.gsc.gsc.bill.service.serviceInterface.BillService;
import com.gsc.gsc.bill.dto.AddBillDTO;
import com.gsc.gsc.bill.dto.BillStatusDTO;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.car.service.serviceImplementation.CarService;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.job_cards.dto.JobCardsDTO;
import com.gsc.gsc.job_cards.service.serviceImplementation.JobCardService;
import com.gsc.gsc.store.service.serviceImplementation.StoreService;
import com.gsc.gsc.user.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;
    @Autowired
    JobCardService jobCardService;
    @Autowired
    CarService carService;
    @Autowired
    StoreService storeService;
    @Autowired
    BillService billService;
    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("activate_car")
    public ResponseEntity updateCarStats(@RequestHeader("Authorization") String token ,@RequestBody ActivateCarDTO activateCarDTO) {
        return adminService.activateCar(token, activateCarDTO);
    }

    @GetMapping("all_job_cards")
    private ResponseEntity getJobCards(
            @RequestHeader("Authorization") String token,
            @RequestHeader("Accept-Language") String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return jobCardService.getJobCards(token, getLangId(lang), pageable);
    }
    @GetMapping("all_job_cards_without_paging")
    private ResponseEntity getJobCards(
            @RequestHeader("Authorization") String token,
            @RequestHeader("Accept-Language") String lang) {

        return jobCardService.getJobCards(token, getLangId(lang));
    }

    @GetMapping("all_users")
    private ResponseEntity getAllUsers(@RequestHeader("Authorization") String token){
        return adminService.getAllUsersForAdmin(token);
    }

    @GetMapping("user/{userId}")
    private ResponseEntity getUserById(@RequestHeader("Authorization") String token , @PathVariable Integer userId){
        return adminService.getUserById(token,userId);
    }
    @GetMapping("all_cars")
    private ResponseEntity getAllCars(@RequestHeader("Authorization") String token){
        return adminService.getAllCarsForAdmin(token);
    }
    @GetMapping("all_cars_by_user_id")
    private ResponseEntity getAllCarsByUserIdForAdmin(@RequestHeader("Authorization") String token , @RequestParam("userId") Integer userId){
        return adminService.getAllCarsByUserIdForAdmin(token,userId);
    }

    @PostMapping(value = "/job_card", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnObject> addJobCardByAdmin(@RequestHeader("Authorization") String token, @RequestBody JobCardsDTO jobCardsDTO) {
        ReturnObject returnObject = jobCardService.createByAdmin(token, jobCardsDTO);
        return ResponseEntity.status(returnObject.isStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(returnObject);
    }

    @PutMapping("/job_card/{jobCardId}")
    public ResponseEntity<ReturnObject> updateJobCardByAdmin(@RequestHeader("Authorization") String token,
                                                             @RequestBody JobCardsDTO jobCardsDTO,
                                                             @PathVariable Integer jobCardId) {
        ReturnObject returnObject = jobCardService.updateByAdmin(token, jobCardsDTO, jobCardId);
        return ResponseEntity.status(returnObject.isStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(returnObject);
    }
    @PutMapping("/job_card/submit/{jobCardId}")
    public ResponseEntity<ReturnObject> submitJobCardByAdmin(@RequestHeader("Authorization") String token,
                                                             @PathVariable Integer jobCardId) {
        ReturnObject returnObject = jobCardService.submitByAdmin(token, jobCardId);
        return ResponseEntity.status(returnObject.isStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(returnObject);
    }

    @PostMapping(value = "bill", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addBillByAdmin(@RequestHeader("Authorization") String token ,@RequestBody AddBillDTO billDTO) {
        return billService.createBillByAdmin(token , billDTO);
    }

    @PutMapping(value = "bill/{billId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateBillByAdmin(@RequestHeader("Authorization") String token,
                                            @RequestBody AddBillDTO billDTO,
                                            @PathVariable Integer billId) {
        return billService.updateBillByAdmin(token, billDTO, billId);
    }
    @PutMapping(value = "billStatus/{billId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateBillStatus(@RequestHeader("Authorization") String token,
                                            @PathVariable Integer billId) {
        return billService.updateBillStatus(token, billId);
    }

    @PostMapping("send_fcm")
    public ResponseEntity sendNotificationToUser(@RequestHeader("Authorization") String token,@RequestBody NotificationDTO notificationDTO) {
        return adminService.sendNotificationByAdmin(token,notificationDTO);
    }

    @GetMapping("all_bills")
    public ResponseEntity getAllUsersBillByToken(
            @RequestHeader("Accept-Language") String langId,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return billService.getBillsForAdminByToken(token, getLangId(langId), pageable);
    }
    @DeleteMapping("car/{id}")
    public ResponseEntity deleteCar(@RequestHeader("Authorization") String token,
                                    @PathVariable Integer id) {
        return carService.deleteCarByAdmin(token,id);
    }
    @PutMapping("billStatus/{billId}")
    public ResponseEntity<?> changeBillStatus(@RequestHeader("Authorization") String token,
                                              @PathVariable Integer billId,
                                              @RequestBody BillStatusDTO billStatusDTO){
        return billService.changeBillStatus(token,billId,billStatusDTO);
    }
    @PostMapping("addCar/{userId}")
    public ResponseEntity<?> addCarByAdmin(@RequestHeader("Authorization") String token,
                                              @PathVariable Integer userId,
                                          @RequestBody CarDTO carDTO){
        return carService.addCarByAdmin(token, userId,carDTO);
    }
    /*


*/
//        return carService.addToFavourite(token,invoiceProductRepository.getProductId());
}