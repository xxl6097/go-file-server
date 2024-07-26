package com.example.bluetoothlibrary.entity;

/**
 * Created by laiyiwen on 2017/5/19.
 */

public class SycnBp {
    int sys;//舒张压
    int dia;//收缩压
    int Hr;//心率

    public int getSys() {
        return sys;
    }

    public void setSys(int sys) {
        this.sys = sys;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getHr() {
        return Hr;
    }

    public void setHr(int hr) {
        Hr = hr;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    String time;




}
