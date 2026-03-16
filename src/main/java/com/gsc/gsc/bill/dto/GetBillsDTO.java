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
    Double  billTotal;
    Double  remainingAmount;

    public GetBillsDTO(Integer id, String referenceNumber, Integer userId, String status,
                       String billStatusTypeCode, Integer createdBy, Double total,
                       Double discount, Date createdAt, String adminNotes, String customerNotes,
                       String carLicenseNumber, String carCode, String billTypeName,
                       String billTypeCode, String billDescription) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.userId = userId;
        this.status = status;
        this.billStatusTypeCode = billStatusTypeCode;
        this.createdBy = createdBy;
        this.total = total;
        this.discount = discount;
        this.createdAt = createdAt;
        this.adminNotes = adminNotes;
        this.customerNotes = customerNotes;
        this.carLicenseNumber = carLicenseNumber;
        this.carCode = carCode;
        this.billTypeName = billTypeName;
        this.billTypeCode = billTypeCode;
        this.billDescription = billDescription;
    }

    public GetBillsDTO(Integer id, String referenceNumber, Integer userId, String status,
                       String billStatusTypeCode, Integer createdBy, Double total,
                       Double discount, Date createdAt, String adminNotes, String customerNotes,
                       String carLicenseNumber, String carCode, String billTypeName,
                       String billTypeCode, String billDescription,
                       String customerName, Integer customerType , Double finalTotalPrice) {

        this.id = id;
        this.referenceNumber = referenceNumber;
        this.userId = userId;
        this.status = status;
        this.billStatusTypeCode = billStatusTypeCode;
        this.createdBy = createdBy;
        this.total = total;
        this.discount = discount;
        this.createdAt = createdAt;
        this.adminNotes = adminNotes;
        this.customerNotes = customerNotes;
        this.carLicenseNumber = carLicenseNumber;
        this.carCode = carCode;
        this.billTypeName = billTypeName;
        this.billTypeCode = billTypeCode;
        this.billDescription = billDescription;
        this.customerName = customerName;
        this.customerType = customerType;
        this.finalTotalPrice = finalTotalPrice;
    }

    String customerName;
    Integer customerType;
    Double finalTotalPrice;
}


