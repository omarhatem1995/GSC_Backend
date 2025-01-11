package com.gsc.gsc.job_cards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddJobCardNotes {
    boolean isForCustomer;
    String notes;
}
