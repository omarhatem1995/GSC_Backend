package com.gsc.gsc.constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnObject {
    String message;
    Object data;
    int id;
    boolean status;
}