package com.gsc.gsc.model;

import com.gsc.gsc.job_cards.dto.JobCardsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_card", schema = "gsc", catalog = "")
public class JobCard {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "code")
    private String code;
    @Column(name = "is_test_drive")
    private Byte isTestDrive;
    @Column(name = "down_payment")
    private Double downPayment;
    @Column(name = "price")
    private Double price;
    @Column(name = "private_notes")
    private String privateNotes;
    @Column(name = "customer_notes")
    private String customerNotes;
    @Column(name = "date")
    private String date;
    @Column(name = "car_id")
    private Integer carId;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "job_card_status_id")
    private Integer jobCardStatusId;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "created_by")
    private Integer createdBy;

    public JobCard(JobCardsDTO jobCardsDTO,Integer statusId, Integer createdById) {
        this.code = jobCardsDTO.getCode();
        this.isTestDrive = jobCardsDTO.getIsTestDrive();
        this.downPayment = jobCardsDTO.getDownPayment();
        this.price = jobCardsDTO.getPrice();
        this.privateNotes = jobCardsDTO.getPrivateNotes();
        this.customerNotes = jobCardsDTO.getCustomerNotes();
        this.date = jobCardsDTO.getDate();
        this.carId = jobCardsDTO.getCarId();
        this.userId = jobCardsDTO.getUserId();
        this.jobCardStatusId = statusId;
        this.createdBy = createdById;
    }
}
