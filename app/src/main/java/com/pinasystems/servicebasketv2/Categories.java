package com.pinasystems.servicebasketv2;


 class Categories {
    int getImagefile() {
        return imagefile;
    }

    void setImagefile(int imagefile) {
        this.imagefile = imagefile;
    }

    //Data Variables
    private int imagefile;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String category;
}