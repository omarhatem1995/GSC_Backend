package com.gsc.gsc.job_cards.controller;

import com.gsc.gsc.admin.service.serviceImplementation.AdminService;
import com.gsc.gsc.job_cards.dto.AddJobCardNotes;
import com.gsc.gsc.job_cards.dto.JobCardsDTO;
import com.gsc.gsc.job_cards.service.serviceImplementation.JobCardService;
import com.gsc.gsc.user.security.util.JwtUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/job_card")
public class JobCardsController {

    @Autowired
    AdminService adminService;
    @Autowired
    JobCardService jobCardService;
    @Autowired
    JwtUtil jwtUtil;



    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity addJobCard(
            @RequestHeader("Authorization") String token,
            @RequestPart("jobCard") JobCardsDTO jobCardsDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return jobCardService.create(token, jobCardsDTO, images);
    }

    @GetMapping("")
    public ResponseEntity<?> findAllJobCards(
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "Accept-Language", required = false) String langId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "searchQuery", required = false) String searchQuery,
            @RequestParam(name = "carId", required = false) Integer carId // ✅ new filter
    ) {
        return jobCardService.getJobCardsForUser(token, getLangId(langId), page, size, searchQuery, carId);
    }
    @PutMapping("notes/{jobCardId}")
    public ResponseEntity addNotesAdmin(@RequestHeader("Authorization") String token,
                                        @PathVariable Integer jobCardId,
                                        @RequestBody AddJobCardNotes addJobCardNotes){
        return jobCardService.addAdminNotes(token,jobCardId,addJobCardNotes);
    }
    @PutMapping("notes/byUser/{jobCardId}")
    public ResponseEntity addCustomerNotes(@RequestHeader("Authorization") String token,
                                        @PathVariable Integer jobCardId,
                                        @RequestBody AddJobCardNotes addJobCardNotes){
        return jobCardService.addCustomerNotes(token,jobCardId,addJobCardNotes);
    }

    @PostMapping("status/update")
    public ResponseEntity updateJobCardStatus(@RequestHeader("Authorization") String token,
                                              @RequestPart("jobCard") JobCardsDTO jobCardsDTO,
                                              @RequestPart(value = "images", required = false) List<MultipartFile> images){
        return jobCardService.updateStatusByUser(token,jobCardsDTO.getJobCardId(),jobCardsDTO,images);
    }

    @GetMapping("status")
    public void ss(){
         jobCardService.getCount();
    }

    @GetMapping("jobCardsByUserId")
    private ResponseEntity getJobCards(
            @RequestHeader("Authorization") String token,
            @RequestHeader("Accept-Language") String lang,
            @RequestParam("userId") Integer userId) {
        return jobCardService.getJobCardsByUserId(token, getLangId(lang),userId);
    }

    @GetMapping("jobCardsDetails")
    private ResponseEntity getJobCardsDetails(
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "Accept-Language",required = false) String lang,
            @RequestParam("jobCardId") Integer jobCardId) {
        return jobCardService.getJobCardsDetails(token, jobCardId);
    }

}