package com.gsc.gsc.model;

import com.gsc.gsc.seller_brand.SellerBrandDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "seller_brand", schema = "gsc", catalog = "")
public class SellerBrand {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_ar")
    private String nameAr;
    @Column(name = "description_en")
    private String descriptionEn;
    @Column(name = "description_ar")
    private String descriptionAr;

    public SellerBrand(SellerBrandDTO sellerBrandDTO) {
        this.code = sellerBrandDTO.getCode();
        this.name = sellerBrandDTO.getName();
        this.description = sellerBrandDTO.getDescription();
        this.imageUrl = sellerBrandDTO.getImageUrl();
    }
}
