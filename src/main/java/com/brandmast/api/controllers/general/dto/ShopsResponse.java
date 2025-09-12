package com.brandmast.api.controllers.general.dto;

public class ShopsResponse {
    private Integer id_shop;
    private String address;
    private String lat;
    private String lon;
    private String name;
    private String zipcode;

    public Integer getId_shop() {
        return id_shop;
    }
    public void setId_shop(Integer id_shop) {
        this.id_shop = id_shop;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }
    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getZipcode() {
        return zipcode;
    }
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
