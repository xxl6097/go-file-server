package com.example.bluetoothlibrary;

import android.app.Application;
import android.os.Handler;

import com.example.bluetoothlibrary.entity.Peripheral;

/**
 * Created by laiyiwen on 2017/4/7.
 */

public class Config extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private Handler myFragmentHandler;
    private Handler popHandler;
    private Peripheral connectPreipheralOpsition;



    public Peripheral getConnectPreipheralOpsition() {
        return connectPreipheralOpsition;
    }

    public void setConnectPreipheralOpsition(Peripheral connectPreipheralOpsition) {
        this.connectPreipheralOpsition = connectPreipheralOpsition;
    }

    public Handler getPopHandler() {
        return popHandler;
    }

    public void setPopHandler(Handler popHandler) {
        this.popHandler = popHandler;
    }

    public Handler getMyFragmentHandler() {
        return myFragmentHandler;
    }

    public void setMyFragmentHandler(Handler myFragmentHandler) {
        this.myFragmentHandler = myFragmentHandler;
    }



}
