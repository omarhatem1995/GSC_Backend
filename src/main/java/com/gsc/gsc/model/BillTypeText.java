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
@Table(name = "bill_type_text", schema = "gsc", catalog = "")
public class BillTypeText {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "d")
    private int d;
    @Basic
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Basic
    @Column(name = "lang_id")
    private int langId;
    @Basic
    @Column(name = "bill_type_id")
    private int billTypeId;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Integer updatedAt;
}
