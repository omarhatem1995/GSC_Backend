package com.gsc.gsc.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPointsDTO {
    Integer totalPoints;
    List<PointsDTO> pointsDTO;
}
