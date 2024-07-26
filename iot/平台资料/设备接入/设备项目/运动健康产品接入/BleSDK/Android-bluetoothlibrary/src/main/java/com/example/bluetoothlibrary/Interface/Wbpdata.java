package com.example.bluetoothlibrary.Interface;

import android.app.Activity;
import android.content.Context;

import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.Impl.ResolveWbp;


/**
 * Created by laiyiwen on 2017/4/28.
 */

public interface Wbpdata {
    void resolveBPData2(byte[] datas, final BluetoothLeClass mBLE, Activity activity);

    void resolveALiBPData(byte[] datas, Context context);


    void setOnWBPDataListener(ResolveWbp.OnWBPDataListener listener);
    void getNowDateTime(BluetoothLeClass mBLE);
    void SendForAll(BluetoothLeClass mBLE);
    void onSingleCommand(BluetoothLeClass mBLE);
     void onStopBleCommand(BluetoothLeClass mBLE);
    void sendUserInfoToBle(BluetoothLeClass mBLE);
}
