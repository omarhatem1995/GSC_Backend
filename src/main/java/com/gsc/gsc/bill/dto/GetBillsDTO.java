package com.gsc.gsc.bill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    String billTypeNameEn;
    String billTypeNameAr;
    String billTypeCode;
    String billDescriptionEn;
    String billDescriptionAr;
    Double billTotal;
    Double remainingAmount;

    // Used by queries that join CBillsStatusText (with langId) — 18 args
    public GetBillsDTO(Integer id, String referenceNumber, Integer userId, String status,
                       String billStatusTypeCode, Integer createdBy, Double total,
                       Double discount, Date createdAt, String adminNotes, String customerNotes,
                       String carLicenseNumber, String carCode,
                       String billTypeNameEn, String billTypeNameAr,
                       String billTypeCode,
                       String billDescriptionEn, String billDescriptionAr) {
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
        this.billTypeNameEn = billTypeNameEn;
        this.billTypeNameAr = billTypeNameAr;
        this.billTypeCode = billTypeCode;
        this.billDescriptionEn = billDescriptionEn;
        this.billDescriptionAr = billDescriptionAr;
    }

    // Used by findByFilters (admin) — 21 args
    public GetBillsDTO(Integer id, String referenceNumber, Integer userId, String status,
                       String billStatusTypeCode, Integer createdBy, Double total,
                       Double discount, Date createdAt, String adminNotes, String customerNotes,
                       String carLicenseNumber, String carCode,
                       String billTypeNameEn, String billTypeNameAr,
                       String billTypeCode,
                       String billDescriptionEn, String billDescriptionAr,
                       String customerName, Integer customerType, Double finalTotalPrice) {
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
        this.billTypeNameEn = billTypeNameEn;
        this.billTypeNameAr = billTypeNameAr;
        this.billTypeCode = billTypeCode;
        this.billDescriptionEn = billDescriptionEn;
        this.billDescriptionAr = billDescriptionAr;
        this.customerName = customerName;
        this.customerType = customerType;
        this.finalTotalPrice = finalTotalPrice;
    }

    String customerName;
    Integer customerType;
    Double finalTotalPrice;
}
