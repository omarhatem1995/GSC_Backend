package com.gsc.gsc.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_model_seller_brand", schema = "gsc", catalog = "")
public class ProductModelSellerBrand {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "product_id")
    private int productId;
    @Column(name = "model_id")
    private int modelId;
    @Column(name = "seller_brand_id")
    private int sellerBrandId;
    @Column(name = "part_no")
    private String partNo;
    @Column(name = "price")
    private Double price;
    @Column(name = "cost")
    private Double cost;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "created_by_id")
    private Long createdById;
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
