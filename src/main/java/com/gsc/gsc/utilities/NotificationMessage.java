package com.gsc.gsc.utilities;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationMessage {
 private String recToken;
 private String title;
 private String body;
 private String image;
 private Map<String,String> data;
}
