package com.gsc.gsc.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gsc.gsc.model.BillProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductBillDTO {
    private Integer id;
    private List<Integer> brandIds;
    private String code;
    private Double price;
    private Integer discountId;
    private Integer promoId;
    private Integer quantity;
    private Integer productId;
    private String imageUrl;
    private String productName;
    private Integer createdBy;

    public ProductBillDTO(Integer id, String code, Double price, Integer discountId, Integer promoId, Integer quantity, Integer productId, String imageUrl, String productName, Integer createdBy) {
        this.id = id;
        this.code = code;
        this.price = price;
        this.discountId = discountId;
        this.promoId = promoId;
        this.quantity = quantity;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.productName = productName;
        this.createdBy = createdBy;
    }

    public ProductBillDTO(BillProduct billProduct) {
        this.code = billProduct.getName();
        this.productId = billProduct.getProductId();
        this.price = billProduct.getPrice();
        this.id = billProduct.getBillId();
        this.quantity = billProduct.getQuantity();
        this.productName = billProduct.getName();
    }
}
