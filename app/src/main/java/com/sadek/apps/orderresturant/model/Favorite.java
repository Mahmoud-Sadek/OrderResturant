package com.sadek.apps.orderresturant.model;

/**
 * Created by Mahmoud Sadek on 12/4/2018.
 */

public class Favorite {
    private String foodId, foodName, foodImage,foodMenuId, foodDescription, foodPrice, foodDiscount, userPhone;

    public Favorite() {
    }

    public Favorite(String foodId, String foodName, String foodImage, String foodMenuId, String foodDescription, String foodPrice, String foodDiscount, String userPhone) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodImage = foodImage;
        this.foodMenuId = foodMenuId;
        this.foodDescription = foodDescription;
        this.foodPrice = foodPrice;
        this.foodDiscount = foodDiscount;
        this.userPhone = userPhone;
    }

    public String getFoodMenuId() {
        return foodMenuId;
    }

    public void setFoodMenuId(String foodMenuId) {
        this.foodMenuId = foodMenuId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public String getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }

    public String getFoodDiscount() {
        return foodDiscount;
    }

    public void setFoodDiscount(String foodDiscount) {
        this.foodDiscount = foodDiscount;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
