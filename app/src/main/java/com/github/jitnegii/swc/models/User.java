package com.github.jitnegii.swc.models;

public class User {

    private String username = "";
    private String email = "";
    private String id = "";
    private String type = "";
    private String state = "";
    private String city = "";


    public User(){

    }

    public User(String username, String email, String id, String type, String state, String city) {
        this.username = username;
        this.email = email;
        this.id = id;
        this.type = type;
        this.state = state;
        this.city = city;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


}
