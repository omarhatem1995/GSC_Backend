package com.gsc.gsc.job_cards.dto;

import com.gsc.gsc.model.JobCard;
import com.gsc.gsc.model.JobCardImages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetJobCardsDTO {
    private Integer id;
    private String code;
    private Byte isTestDrive;
    private Double downPayment;
    private Double price;
    private String privateNotes;
    private String customerNotes;
    private String date;
    private Integer carId;
    private String carData;
    private Integer userId;
    private Integer jobCardStatusId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer createdBy;
    private List<String> listOfJobCardsImages;
    private List<JobCardNotesDTO> jobCardNotes;

    public GetJobCardsDTO(JobCard jobCardsDTO, List<JobCardImages> jobCardImages,String carData,List<JobCardNotesDTO> jobCardNotes) {
        this.id = jobCardsDTO.getId();
        this.code = jobCardsDTO.getCode();
        this.isTestDrive = jobCardsDTO.getIsTestDrive();
        this.downPayment = jobCardsDTO.getDownPayment();
        this.price = jobCardsDTO.getPrice();
        this.privateNotes = jobCardsDTO.getPrivateNotes();
        this.customerNotes = jobCardsDTO.getCustomerNotes();
        this.date = jobCardsDTO.getDate();
        this.carId = jobCardsDTO.getCarId();
        this.carData = carData;
        this.userId = jobCardsDTO.getUserId();
        this.jobCardStatusId = jobCardsDTO.getJobCardStatusId();
        this.createdBy = jobCardsDTO.getCreatedBy();
        this.createdAt = jobCardsDTO.getCreatedAt();
        this.updatedAt = jobCardsDTO.getUpdatedAt();
        this.listOfJobCardsImages = new ArrayList<>();
        this.jobCardNotes = jobCardNotes;
        if(jobCardImages != null && !jobCardImages.isEmpty()) {
            for (int i = 0; i < jobCardImages.size(); i++) {
                listOfJobCardsImages.add(jobCardImages.get(i).getUrl());
            }
        }
    }
}
