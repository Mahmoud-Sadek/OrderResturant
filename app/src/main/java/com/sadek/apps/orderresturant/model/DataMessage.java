package com.sadek.apps.orderresturant.model;

import java.util.Map;

/**
 * Created by Mahmoud Sadek on 12/6/2018.
 */

public class DataMessage {
    public String to;
    public Map<String,String> data;

    public DataMessage() {
    }

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
