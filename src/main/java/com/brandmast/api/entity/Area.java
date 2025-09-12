package com.brandmast.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "areas")
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area")
    private Integer idArea;

    @Column(name = "area_name")
    private String area_name;

    //Area ID
    public Integer getId_area() {
        return idArea;
    }
    public void setId_area(Integer id_area) {
        this.idArea = id_area;
    }

    //Area Name
    public String getArea_name() {
        return area_name;
    }
    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }
}
