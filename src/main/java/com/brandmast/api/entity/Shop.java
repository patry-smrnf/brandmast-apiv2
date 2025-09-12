package com.brandmast.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_shop", updatable = false, nullable = false)
    private Integer idShop;

    @Column(name = "address")
    private String address;

    @Column(name = "lat")
    private String lat;

    @Column(name = "lon")
    private String lon;

    @Column(name = "name")
    private String name;

    @Column(name = "zipcode")
    private String zipcode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_team", nullable = true)
    private Team team;

    //Shop ID
    public Integer getIdShop() {
        return idShop;
    }
    public void setIdShop(Integer idShop) {
        this.idShop = idShop;
    }

    //Shop Address
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    //Shop Lat
    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }

    //Shop Lon
    public String getLon() {
        return lon;
    }
    public void setLon(String lon) {
        this.lon = lon;
    }

    //Shop Name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    //Shop ZipCode
    public String getZipcode() {
        return zipcode;
    }
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    //Shop Team
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

}
