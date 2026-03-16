package com.gsc.gsc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_vehicle", schema = "gsc", catalog = "")
public class ProductVehicle {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "product_id")
    private int productId;
    @Basic
    @Column(name = "model_id")
    private int modelId;
    @Basic
    @Column(name = "year_from")
    private Integer yearFrom;
    @Basic
    @Column(name = "year_to")
    private Integer yearTo;

}
