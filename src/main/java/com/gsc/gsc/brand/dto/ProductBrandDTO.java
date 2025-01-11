package com.gsc.gsc.brand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductBrandDTO {
    private int id;
    private int brandId;
    private int productId;
    private Double price;
    private Integer quantity;
    private String info;
    private String imageUrl;
    private String partNo;
}
