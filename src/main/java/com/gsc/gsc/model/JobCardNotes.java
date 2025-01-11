package com.gsc.gsc.model;

import com.gsc.gsc.job_cards.dto.JobCardsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_card_notes", schema = "gsc", catalog = "")
public class JobCardNotes {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "job_card_id")
    private Integer jobCardId;
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
