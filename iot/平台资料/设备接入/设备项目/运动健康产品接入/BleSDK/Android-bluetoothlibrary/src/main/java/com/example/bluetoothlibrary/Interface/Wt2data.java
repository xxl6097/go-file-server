package com.example.bluetoothlibrary.Interface;

import android.app.Activity;

import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.Config;
import com.example.bluetoothlibrary.Impl.ResolveWt2;

/**
 * Created by laiyiwen on 2017/4/28.
 */

public interface Wt2data {
    /**
     * 这个适用于数据解析的接口
     * @param datas
     * @param mBLE
     * @param activity
     *
     */
    void calculateData_WT2(byte[] datas, BluetoothLeClass mBLE, Activity activity);

    /**
     *  发送命令校准设备时间。。。
     * @param mBLE
     */
    void SendForTime(BluetoothLeClass mBLE);


    /**
     * 如果收到就3秒收到就撤销,没有收到就继续发
     * @param mBLE
     */
    void MyTimeTask(final BluetoothLeClass mBLE);

    /**
     * 发送命令获取体温计数据存储状态
     * @param mBLE
     */



    /**
     * 接收数据
     * @param
     */
    void setOnWt2DataListener(ResolveWt2.OnWt2DataListener listener);
}
