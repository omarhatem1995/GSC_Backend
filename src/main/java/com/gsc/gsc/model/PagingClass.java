package com.gsc.gsc.model;

import lombok.Data;
import org.springframework.data.domain.Pageable;

@Data
public class PagingClass {
    private int pageNumber;
    private int pageSize;
    // additional properties if needed
}
