package com.sadek.apps.orderresturant.model;

import com.google.android.gms.location.places.Place;

import java.util.HashMap;

/**
 * Created by Mahmoud Sadek on 5/3/2018.
 */

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String IsStuff;
    private HashMap HomeAddress;

    public User() {
    }

    public User(String name, String password, String phone, String isStuff, HashMap homeAddress) {
        Name = name;
        Password = password;
        Phone = phone;
        IsStuff = isStuff;
        HomeAddress = homeAddress;
    }

    public User(String name, String password, String phone) {
        Name = name;
        Password = password;
        Phone = phone;
        IsStuff = "false";
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getIsStuff() {
        return IsStuff;
    }

    public void setIsStuff(String isStuff) {
        IsStuff = isStuff;
    }

    public HashMap getHomeAddress() {
        return HomeAddress;
    }

    public void setHomeAddress(HashMap homeAddress) {
        HomeAddress = homeAddress;
    }
}
