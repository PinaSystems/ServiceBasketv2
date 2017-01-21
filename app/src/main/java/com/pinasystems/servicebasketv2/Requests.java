package com.pinasystems.servicebasketv2;

class Requests {
    //Data Variables
    private String name;
    private String date;
    private String subcategory;
    private String reqid;
    private String description;

    //Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    String getReqid() {
        return reqid;
    }

    void setReqid(String reqid) {
        this.reqid = reqid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}