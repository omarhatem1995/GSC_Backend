package com.gsc.gsc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllProductDetails {
    Integer id;
    String code;
    String name;
    String description;
    Double price;
    String url;
    String discountId;
    String discountCode;
    String discountTypeId;
    String discountTypeCode;
}
