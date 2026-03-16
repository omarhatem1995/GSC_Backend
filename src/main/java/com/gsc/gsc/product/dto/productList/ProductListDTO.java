package com.gsc.gsc.product.dto.productList;

import com.gsc.gsc.product.dto.ProductManufacturerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDTO {
    private Integer id;
    private String code;
    private String nameEn;
    private String nameAr;
    private String descriptionEn;
    private String descriptionAr;
    private String imageUrl;
    private Double price;
    private Double cost;
    private Integer discountId;
    private Integer promoId;
    private Integer productTypeId;
    private List<ProductManufacturerDTO> manufacturers = new ArrayList<>();
}
