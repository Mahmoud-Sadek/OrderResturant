package com.sadek.apps.orderresturant.model;

/**
 * Created by Mahmoud Sadek on 5/3/2018.
 */

public class Category {
    private String Name;
    private String Image;

    public Category(String name, String image) {
        Name = name;
        Image = image;
    }

    public Category() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
