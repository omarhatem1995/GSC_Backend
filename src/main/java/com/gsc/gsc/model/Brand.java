package com.gsc.gsc.model;

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
public class Brand {
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

}
