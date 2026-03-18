package com.gsc.gsc.point.controller;

import com.gsc.gsc.point.dto.AddPointsDTO;
import com.gsc.gsc.point.service.serviceImplementation.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gsc.gsc.utilities.Utilities.getLangId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/point")
public class PointController {
    @Autowired
    PointService pointService;

    @PostMapping("admin/{userId}")
    public ResponseEntity addPointsByAdmin(@RequestHeader("Authorization") String token,
                                           @RequestHeader(value = "Accept-Language", required = false) String lang,
                                           @PathVariable("userId") Integer userId,
                                           @RequestBody AddPointsDTO addPointsDTO){
        return pointService.addPointsToUserFromAdmin(token,getLangId(lang),userId,addPointsDTO);
    }

    @DeleteMapping("admin/{pointsId}")
    public ResponseEntity deletePointsByAdmin(@RequestHeader("Authorization") String token,
                                              @RequestHeader("Accept-Language") String lang,
                                              @PathVariable("pointsId") Integer pointsId){
            return pointService.deletePointsByAdmin(token,getLangId(lang),pointsId);
    }
}
