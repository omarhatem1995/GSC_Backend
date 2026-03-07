package com.gsc.gsc.job_cards.dto;

import com.gsc.gsc.bill.dto.OtherProductDTO;
import com.gsc.gsc.bill.dto.ProductBillDTO;
import com.gsc.gsc.model.JobCardNotes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCardsDTO {
    private Integer jobCardId;
    private String code;
    private Byte isTestDrive = 0;
    private Double price;
    private Double downPayment;
    private String privateNotes;
    private String customerNotes;
    private String customerMobileVersion;
    private String customerMobileMacAddress;
    private String date;
    private Integer carId;
    private String carData;
    private Integer userId;
    private List<String> jobCardsUrl;
    private List<JobCardNotesDTO> jobCardNotes;
    private String jobCardStatus;
    private List<ProductBillDTO> productBillDTOList;
    private List<OtherProductDTO> otherProductsDTOList;
}
