package com.gsc.gsc.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultResponseDTO {
    private int id;
    private String message;
    private Integer userId;

    public DefaultResponseDTO(int id, String message, Integer currentStep, Integer requestStatus) {
        this.id = id;
        this.message = message;

    }

    public DefaultResponseDTO(int id, String message)
    {
        this.id = id;
        this.message = message;
    }


}