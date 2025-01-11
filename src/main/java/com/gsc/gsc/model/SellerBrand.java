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
    @Basic
    @Column(name = "code")
    private String code;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "description")
    private String description;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Basic
    @Column(name = "image_url")
    private String imageUrl;

    public SellerBrand(SellerBrandDTO sellerBrandDTO) {
        this.code = sellerBrandDTO.getCode();
        this.name = sellerBrandDTO.getName();
        this.description = sellerBrandDTO.getDescription();
        this.imageUrl = sellerBrandDTO.getImageUrl();
    }
}
