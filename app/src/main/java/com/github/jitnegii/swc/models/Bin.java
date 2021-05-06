package com.github.jitnegii.swc.models;

public class Bin {

    private String latitude = "";
    private String longitude = "";
    private String status = "";

    public Bin(){

    }

    public Bin(String latitude, String longitude, String status) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}
