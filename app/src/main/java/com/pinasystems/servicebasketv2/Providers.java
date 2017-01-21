package com.pinasystems.servicebasketv2;


class Providers {
    private String est_date;
    private String est_time;
    private String min_price;
    private String max_price;
    private String comment;
    private String rating;

    String getRating() {
        return rating;
    }

    void setRating(String rating) {
        this.rating = rating;
    }

    String getComment() {
        return comment;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    String getEst_date() {
        return est_date;
    }

    void setEst_date(String est_date) {
        this.est_date = est_date;
    }

    String getEst_time() {
        return est_time;
    }

    void setEst_time(String est_time) {
        this.est_time = est_time;
    }

    String getMin_price() {
        return min_price;
    }

    void setMin_price(String min_price) {
        this.min_price = min_price;
    }

    public String getMax_price() {
        return max_price;
    }

    public void setMax_price(String max_price) {
        this.max_price = max_price;
    }
}
