package com.gsc.gsc.model.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_details_view", schema = "gsc", catalog = "")
public class ProductDetailsView {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "code")
    private String code;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "discount_id")
    private Integer discountId;

    @Column(name = "promo_id")
    private Integer promoId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "promo_code")
    private String promoCode;

    @Column(name = "discount_code")
    private String discountCode;

    @Column(name = "discount_type_code")
    private String discountTypeCode;

    @Column(name = "product_type_id")
    private Integer productTypeId;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "lang_id")
    private Integer langId;

}
