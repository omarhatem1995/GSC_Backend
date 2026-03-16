package com.gsc.gsc.bill.dto;

import com.gsc.gsc.model.BillProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherProductDTO {
    Integer id;
    String productName;
    Integer quantity;
    Double discount;
    String price;
    Integer createdBy;

    public OtherProductDTO(Integer id, Integer quantity, String name,String price,Integer createdBy){
        this.id = id;
        this.quantity = quantity;
        this.productName = name;
        this.price = price;
        this.createdBy = createdBy;
    }

    public OtherProductDTO(BillProduct billProduct) {
        this.price = String.valueOf(billProduct.getPrice());
        this.quantity = billProduct.getQuantity();
        this.productName  = billProduct.getName();
        this.discount  = billProduct.getDiscount();
    }
}
