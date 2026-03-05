package com.gsc.gsc.bill.dto;

import com.gsc.gsc.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddBillDTO {
    Integer userId;
    String userName;
    List<ProductBillDTO> productBillDTOList;
    List<OtherProductDTO> otherProductsDTOList;
    Double total;
    String date;
    String notes;
    String privateNotes;
    Double discount;
    Integer billTypeId;
    String referenceNumber;
    Double downPayment;
    String carData;
}
