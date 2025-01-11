package com.gsc.gsc.model;

import com.gsc.gsc.bill.dto.OtherProductDTO;
import com.gsc.gsc.bill.dto.ProductBillDTO;
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
@Table(name = "job_card_product", schema = "gsc", catalog = "")
public class JobCardProduct {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "job_card_id")
    private int jobCardId;
    @Column(name = "product_id")
    private Integer productId;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private String price;
    @Column(name = "customer_mobile_version")
    private String customerMobileVersion;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "customer_approved_at")
    private Timestamp customerApprovedAt;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    public JobCardProduct(OtherProductDTO otherProductDTO,Integer jobCardId,Integer userId) {
        this.jobCardId = jobCardId;
        this.quantity = otherProductDTO.getQuantity();
        this.productId = null;
        this.name = otherProductDTO.getProductName();
        this.price = otherProductDTO.getPrice();
        this.createdBy = userId;
    }

    public JobCardProduct(ProductBillDTO productBillDTO, Integer id,Integer userId) {
        this.jobCardId = id;
        this.quantity = productBillDTO.getQuantity();
        this.productId = productBillDTO.getProductId();
        this.price = String.valueOf(productBillDTO.getPrice());
        this.createdBy = userId;
    }
}
