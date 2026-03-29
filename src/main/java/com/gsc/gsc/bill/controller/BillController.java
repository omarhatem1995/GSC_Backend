package com.gsc.gsc.bill.controller;

import com.gsc.gsc.bill.dto.AddBillDTO;
import com.gsc.gsc.bill.pdf.BillPdfGeneratorITextService;
import com.gsc.gsc.bill.service.serviceImplementation.BillService;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.job_cards.dto.AddJobCardNotes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/bill")
public class BillController {

    @Autowired
    BillService billService;
    @Autowired
    BillPdfGeneratorITextService billPdfGeneratorITextService;

    @GetMapping("/pdf2/{billId}")
    public ResponseEntity<ReturnObject> generateBillPdf(@RequestHeader("Authorization") String token, @PathVariable int billId, @RequestParam String macAddress,
                                                        @RequestParam(value = "includePrivateNotes",required = false) Boolean includePrivateNotes) throws IOException {
        // Locate your PDF file
        ReturnObject returnObject = billPdfGeneratorITextService.exportIText2(token,billId, macAddress,includePrivateNotes);

        if (returnObject.isStatus()) {
            Object data = returnObject.getData();

            // Log the class name of the data to confirm its type
            System.out.println("Data class: " + data.getClass().getName());
            System.out.println("Data: " + data);

            // Check if the data is a String (file path)
            if (data instanceof String) {
                String filePath = (String) data;

                // Read the file content into a byte array
                byte[] pdfBytes = Files.readAllBytes(Paths.get(filePath));

                // Convert the byte array to a Base64 string
                String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
                returnObject.setData("Inv_"+billId);
                // Return the Base64 encoded PDF string
                return new ResponseEntity<>(returnObject, HttpStatus.OK);
            } else {
                // If the data is not a valid file path or byte array, return an error message
                return new ResponseEntity<>(returnObject, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            if ("Unauthorized".equals(returnObject.getMessage())) {
                return new ResponseEntity<>(returnObject, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(returnObject, HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("user/all_bills")
    public ResponseEntity getAllBillsByToken(@RequestHeader("Accept-Language")String langId,@RequestHeader("Authorization") String token) {
        return billService.getAllBillsByToken(token,getLangId(langId));
    }

    @GetMapping("billsDetails")
    public ResponseEntity getBillsData(@RequestHeader("Authorization") String token,
                                       @RequestHeader(value = "Accept-Language",required = false) String langId,
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
        return billService.createProductBill(token , billDTO);
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

    @GetMapping("allBills")
    public ResponseEntity<?> getBills(
            @RequestHeader(value = "Accept-Language",required = false) String langId,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer carId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String billNumber,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return billService.getBills(
                token,
                userId,
                carId,
                search,
                billNumber,
                fromDate,
                toDate,
                getLangId(langId),
                pageable
        );
    }
}