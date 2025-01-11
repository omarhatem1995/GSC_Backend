package com.gsc.gsc.bill.dto;

import com.gsc.gsc.model.Bill;
import com.gsc.gsc.model.Product;
import com.gsc.gsc.model.view.ProductDetailsView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillProductsDTO {
    private List<ProductDetailsView> products;
    private Bill bill;
}
