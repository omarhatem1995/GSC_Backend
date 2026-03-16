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
@Table(name = "bill_product", schema = "gsc", catalog = "")
public class BillProduct {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "product_id", nullable = false, columnDefinition = "int default 999999")
    private Integer productId;
    @Column(name = "bill_id")
    private Integer billId;
    @Column(name = "product_manufacturer_id")
    private Integer productManufacturerId;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "price")
    private Double price;
    @Column(name = "discount")
    private Double discount;
    @Column(name = "name")
    private String name;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "customer_mobile_version")
    private String customerMobileVersion;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "customer_approved_at")
    private Timestamp customerApprovedAt;

}
