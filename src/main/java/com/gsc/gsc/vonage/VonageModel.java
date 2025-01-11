package com.gsc.gsc.vonage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VonageModel {
    String sms;
    String requestCode;
    String phone;
}
