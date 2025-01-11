package com.gsc.gsc.point.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPointsDTO {

    private Integer userId;
    private Integer points;
    private String reason;
}
