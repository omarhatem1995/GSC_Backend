package com.gsc.gsc.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetStoresDTO {
    Integer id;
    Date createdAt;
    String name;
    String description;
    String imageUrl;
    Integer storeId;

}
