package com.gsc.gsc.configurations.dto.seller_brand;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerBrandsDTO {
    Integer brandId;
    String brandName;
    String brandDescription;
}
