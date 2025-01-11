package com.gsc.gsc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_seller_brand", schema = "gsc", catalog = "")
public class ProductSellerBrand {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "seller_brands_id")
    private int sellerBrandsId;
    @Basic
    @Column(name = "product_id")
    private int productId;

}
