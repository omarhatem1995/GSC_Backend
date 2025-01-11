package com.gsc.gsc.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationDTO {
    List<Integer> userIds;
    String title;
    String body;
}
