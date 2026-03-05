package com.gsc.gsc.model;

import com.gsc.gsc.inventory.dto.CreateProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "code")
    private String code;
    @Column(name = "price")
    private Double price;
    @Column(name = "cost")
    private Double cost;
    @Column(name = "discount_id")
    private Integer discountId;
    @Column(name = "promo_id")
    private Integer promoId;
    @Column(name = "product_type_id")
    private Integer productTypeId = 0;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_ar")
    private String nameAr;
    @Column(name = "description_en")
    private String descriptionEn;
    @Column(name = "description_ar")
    private String descriptionAr;
    @Column(name = "image_url")
    private String imageUrl;

    public Product(CreateProductDTO dto,String imageUrl){
        this.price = dto.getPrice();
        this.cost = dto.getCost();
        this.code = dto.getPartNo();
        this.nameEn = dto.getProductNameEn();
        this.nameAr = dto.getProductNameAr();
        this.descriptionEn = dto.getProductDescriptionEn();
        this.descriptionAr = dto.getProductDescriptionAr();
        this.imageUrl = imageUrl;
    }
}
