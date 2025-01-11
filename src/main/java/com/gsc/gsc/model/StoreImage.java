package com.gsc.gsc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "store_image", schema = "gsc", catalog = "")
public class StoreImage {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "url")
    private String url;
    @Basic
    @Column(name = "counter")
    private int counter;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Basic
    @Column(name = "store_id")
    private Integer storeId;


    public StoreImage(Integer id, String url){
        this.id = id;
        this.url = url;
        this.counter = 1;
        this.storeId = 1;
    }

}
