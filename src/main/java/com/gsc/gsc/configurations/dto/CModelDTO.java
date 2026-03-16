package com.gsc.gsc.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CModelDTO {
        private Integer id;
        private String code;
        private List<Integer> years;
        private List<CColorDTO> colors;
}
