package com.gsc.gsc.product.dto;

import com.gsc.gsc.configurations.dto.seller_brand.SellerBrandsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProductsDTO {
    private Integer id;
    private String code;
    private String imageUrl;
    private Double price;
    private String productName;
    private String productDescription;
    private List<Integer> brandIds;
    private List<SellerBrandsDTO> brands;
    public GetProductsDTO(Integer id, String code, String imageUrl,Double price,
                          Integer brandId,String productName,String productDescription) {
        this.id = id;
        this.code = code;
        this.imageUrl = imageUrl;
        this.price = price;
        this.brandIds = new ArrayList<>();
        addBrandId(brandId);
        this.productName = productName;
        this.productDescription = productDescription;

    }
    public GetProductsDTO(Integer id, String code, String imageUrl,Double price, String productName,String productDescription) {
        this.id = id;
        this.code = code;
        this.imageUrl = imageUrl;
        this.price = price;
        this.brandIds = new ArrayList<>();
//        addBrandId(brandId);
        this.productName = productName;
        this.productDescription = productDescription;

    }
    public void addBrandId(Integer brandId) {
        if (!this.brandIds.contains(brandId)) {
            this.brandIds.add(brandId);
        }
    }}
