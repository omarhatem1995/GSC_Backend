package com.gsc.gsc.model;

import com.gsc.gsc.configurations.dto.CreateManufacturerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

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
    @Column(name = "created_by")
    private Integer createdBy;

    public SellerBrand(CreateManufacturerDTO createManufacturerDTO) {
        this.code = createManufacturerDTO.getCode();
        this.nameEn = createManufacturerDTO.getNameEn();
        this.nameAr = createManufacturerDTO.getNameAr();
        this.descriptionEn = createManufacturerDTO.getNameEn();
        this.descriptionAr = createManufacturerDTO.getDescriptionAr();
    }
}
