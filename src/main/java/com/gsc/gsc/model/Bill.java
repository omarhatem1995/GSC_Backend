package com.gsc.gsc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bill {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "total")
    private Double total;
    @Column(name = "discount")
    private Double discount;
    @Column(name = "discount_type")
    private String discountType;
    @Column(name = "downPayment")
    private Double downPayment;
    @Column(name = "user_id")
    private int userId;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "reference_number")
    private String referenceNumber;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "status_id")
    private Integer statusId;
    @Column(name = "bill_type_id")
    private Integer billTypeId;
    @Column(name = "admin_notes")
    private String adminNotes;
    @Column(name = "customer_notes")
    private String customerNotes;
    @Column(name = "car_id")
    private Integer carId;
    @Column(name = "selected_date")
    private String date;
    @Column(name = "final_total_price")
    private Double finalTotalPrice;

}
