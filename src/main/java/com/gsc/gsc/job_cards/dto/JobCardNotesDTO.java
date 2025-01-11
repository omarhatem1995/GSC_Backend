package com.gsc.gsc.job_cards.dto;

import com.gsc.gsc.model.JobCardNotes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCardNotesDTO {
    private String message;
    private Boolean isPrivate = false;
    private String createdUser;
    private String createdUserType;
    private Timestamp createdAt;

    public JobCardNotesDTO(JobCardNotes jobCardNotes , String createdUserName,String createdUserType){
        this.isPrivate = jobCardNotes.getIsPrivate();
        this.message = jobCardNotes.getMessage();
        this.createdAt = jobCardNotes.getCreatedAt();
        this.createdUserType = createdUserType;
        this.createdUser = createdUserName;
    }

}
