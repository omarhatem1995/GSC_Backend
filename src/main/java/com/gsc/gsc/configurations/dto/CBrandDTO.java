package com.gsc.gsc.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBrandDTO {
    private Integer id;
    private String code;
    private String imageUrl;
    private String name;
    private String description;
    private List<CModelDTO> models;

    public CBrandDTO(Integer id, String code, String imageUrl, String name, String description) {
        this.id = id;
        this.code = code;
        this.imageUrl = imageUrl;
        this.name = name;
        this.description = description;
    }
}


