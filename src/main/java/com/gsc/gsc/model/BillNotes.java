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
@Table(name = "bill_notes", schema = "gsc", catalog = "")
public class BillNotes {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "bill_id")
    private Integer billId;
    @Column(name = "message")
    private String message;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "is_private")
    private Boolean isPrivate = false;
    @Column(name = "customer_mobile_version")
    private String customerMobileVersion;
    @Column(name = "approved_by_customer_at")
    private Timestamp approvedByCustomerAt;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
