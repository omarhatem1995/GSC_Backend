package com.gsc.gsc.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "oe_number", schema = "gsc", catalog = "")
public class OeNumber {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "oe_number")
    private String oeNumber;
    @Basic
    @Column(name = "brand_id")
    private int brandId;
    @Basic
    @Column(name = "product_id")
    private int productId;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOeNumber() {
        return oeNumber;
    }

    public void setOeNumber(String oeNumber) {
        this.oeNumber = oeNumber;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OeNumber oeNumber1 = (OeNumber) o;
        return id == oeNumber1.id && brandId == oeNumber1.brandId && productId == oeNumber1.productId && Objects.equals(oeNumber, oeNumber1.oeNumber) && Objects.equals(createdAt, oeNumber1.createdAt) && Objects.equals(updatedAt, oeNumber1.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, oeNumber, brandId, productId, createdAt, updatedAt);
    }
}
