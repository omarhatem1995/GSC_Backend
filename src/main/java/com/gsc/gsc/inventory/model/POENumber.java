package com.gsc.gsc.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "p_oe_number", schema = "gsc", catalog = "")
public class POENumber {
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Id
        @Column(name = "id")
        private Long id;
        @Column(name = "created_by_id")
        private Integer createdById;
        @Column(name = "code")
        private String code;
        @Column(name = "product_brand_seller_brand_id")
        private Long productBrandSellerBrandId;
}
