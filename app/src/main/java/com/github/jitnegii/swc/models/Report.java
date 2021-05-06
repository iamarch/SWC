package com.github.jitnegii.swc.models;

public class Report {

    private String username = "";
    private String text = "";
    private String image = "";
    private String time = "";
    private String id = "";
    private String location = "";

    public Report() {

    }

    public Report(String username, String text, String image, String time, String id,String location) {
        this.username = username;
        this.text = text;
        this.image = image;
        this.time = time;
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
