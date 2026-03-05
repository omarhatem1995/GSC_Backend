package com.gsc.gsc.inventory.model;

import com.google.type.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "store_product_model_seller_brand", schema = "gsc", catalog = "")
public class StoreProductBrandSellerBrand {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "product_model_seller_brand_id")
    private Long productBrandSellerBrandId;
    @Column(name = "store_id")
    private int storeId;
    @Column(name = "created_by_id")
    private int createdById;
    @Column(name = "created_at")
    private DateTime createdAt;
    @Column(name = "updated_at")
    private DateTime updatedAt;

}
