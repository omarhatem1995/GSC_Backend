package com.gsc.gsc.bill.controller;

import com.gsc.gsc.bill.dto.AddBillDTO;
import com.gsc.gsc.bill.service.serviceInterface.BillService;
import com.gsc.gsc.job_cards.dto.AddJobCardNotes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/bill")
public class BillController {

    @Autowired
    BillService billService;

    @GetMapping("user/all_bills")
    public ResponseEntity getAllBillsByToken(@RequestHeader("Accept-Language")String langId,@RequestHeader("Authorization") String token) {
        return billService.getAllBillsByToken(token,getLangId(langId));
    }

    @GetMapping("billsDetails")
    public ResponseEntity getBillsData(@RequestHeader("Authorization") String token,
                                       @RequestHeader("Accept-Language") String langId,
                                       @RequestParam("billId") Integer billId) {
        return billService.getBillsData(billId);
    }

    @GetMapping("billsByUserId")
    public ResponseEntity billsByUserId(@RequestHeader("Authorization") String token,
                                        @RequestHeader("Accept-Language") String langId,
                                        @RequestParam("userId") Integer userId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return billService.getBillsForAdminByTokenForUserId(token,getLangId(langId),userId,pageable);
    }

    @PostMapping("")
    public ResponseEntity addBill(@RequestHeader("Authorization") String token ,@RequestBody AddBillDTO billDTO) {
        return billService.createBill(token , billDTO);
    }
    @PutMapping("notes/{billId}")
    public ResponseEntity addNotesAdmin(@RequestHeader("Authorization") String token,
                                        @PathVariable Integer billId,
                                        @RequestBody AddJobCardNotes addJobCardNotes){
        return billService.addAdminNotes(token,billId,addJobCardNotes);
    }
    @PutMapping("notes/byUser/{billId}")
    public ResponseEntity addCustomerNotes(@RequestHeader("Authorization") String token,
                                           @PathVariable Integer billId,
                                           @RequestBody AddJobCardNotes addJobCardNotes){
        return billService.addCustomerNotes(token,billId,addJobCardNotes);
    }
}