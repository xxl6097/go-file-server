package com.example.bluetoothlibrary.entity;

/**
 * Created by badcode on 16/2/18.
 */
public class SleepBlock {
    private short[] r;
    private short spo2, pr, rr;
    long t;

    public SleepBlock(short[] r, short spo2, short pr, short rr, long t) {
        this.r = r;
        this.spo2 = spo2;
        this.pr = pr;
        this.rr = rr;
        this.t = t;
    }

    public short[] getR() {
        return r;
    }

    public void setR(short[] r) {
        this.r = r;
    }

    public short getSpo2() {
        return spo2;
    }

    public void setSpo2(short spo2) {
        this.spo2 = spo2;
    }

    public short getPr() {
        return pr;
    }

    public void setPr(short pr) {
        this.pr = pr;
    }

    public short getRr() {
        return rr;
    }

    public void setRr(short rr) {
        this.rr = rr;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }
}
