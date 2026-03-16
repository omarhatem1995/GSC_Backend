package com.gsc.gsc.configurations.dto;

import com.gsc.gsc.model.SellerBrand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerBrandDTO {
    private Integer id;
    private String name;
    private String description;

    public SellerBrandDTO(SellerBrand sellerBrand, Integer lang) {
        this.id = sellerBrand.getId();
        if (lang == 1) {
            this.name = sellerBrand.getNameEn();
            this.description = sellerBrand.getDescriptionEn();
        }else {
            this.name = sellerBrand.getNameAr();
            this.description = sellerBrand.getDescriptionAr();
        }
    }
}
