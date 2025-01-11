package com.gsc.gsc.constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnObjectPaging {
    String message;
    Object data;
    int totalPages;
    Long totalCount;
    int id;
    boolean status;
}