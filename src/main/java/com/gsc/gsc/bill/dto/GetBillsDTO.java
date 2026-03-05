package com.gsc.gsc.bill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetBillsDTO {
    Integer id;
    String referenceNumber;
    Integer userId;
    String status;
    String billStatusTypeCode;
    Integer createdBy;
    Double total;
    Double discount;
    Date createdAt;
    String adminNotes;
    String customerNotes;
    String carLicenseNumber;
    String carCode;
    String billTypeName;
    String billTypeCode;
    String billDescription;
}


