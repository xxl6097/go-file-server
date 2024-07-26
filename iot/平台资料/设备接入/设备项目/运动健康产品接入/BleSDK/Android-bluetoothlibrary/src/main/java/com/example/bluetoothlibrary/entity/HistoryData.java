package com.example.bluetoothlibrary.entity;

import java.io.Serializable;

/**
 * Created by badcode on 15/10/21.
 */
public class HistoryData implements Serializable {

    private String date;
    private int SpO2;
    private int HeartRate;
    private int resp;

    public HistoryData(){

    }

    public HistoryData(String d, int s, int h, int r) {
        date = d;
        SpO2 = s;
        HeartRate = h;
        resp = r;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSpO2() {
        return SpO2;
    }

    public void setSpO2(int spO2) {
        SpO2 = spO2;
    }

    public int getHeartRate() {
        return HeartRate;
    }

    public void setHeartRate(int heartRate) {
        HeartRate = heartRate;
    }

    public int getResp() {
        return resp;
    }

    public void setResp(int resp) {
        this.resp = resp;
    }
}
