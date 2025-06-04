package com.gsc.gsc.bill.dto;

import com.gsc.gsc.model.BillProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductBillDTO {
    private Integer id;
    private String code;
    private Double price;
    private Integer discountId;
    private Integer promoId;
    private Integer quantity;
    private Integer productId;
    private String imageUrl;
    private String productName;
    private Integer createdBy;

    public ProductBillDTO(BillProduct billProduct) {
        this.code = billProduct.getName();
        this.productId = billProduct.getProductId();
        this.price = billProduct.getPrice();
        this.id = billProduct.getBillId();
        this.quantity = billProduct.getQuantity();
        this.productName = billProduct.getName();
    }
}
