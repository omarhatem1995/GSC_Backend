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
@Table(name = "product_type", schema = "gsc", catalog = "")
public class ProductType {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name_en")
    private String nameEn;
    @Basic
    @Column(name = "name_ar")
    private String nameAr;
    @Basic
    @Column(name = "description_en")
    private String descriptionEn;
    @Basic
    @Column(name = "description_ar")
    private String descriptionAr;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
