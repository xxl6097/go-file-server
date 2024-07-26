package com.example.bluetoothlibrary.entity;

import java.io.Serializable;

/**
 * Created by laiyiwen on 2017/5/12.
 */

public class SycnData implements Serializable {
    private String TempID;//时间id
    private String Temp;//温度值

    public String getTempID() {
        return TempID;
    }

    public void setTempID(String tempID) {
        TempID = tempID;
    }

    public String getTemp() {
        return Temp;
    }

    public void setTemp(String temp) {
        Temp = temp;
    }
}
