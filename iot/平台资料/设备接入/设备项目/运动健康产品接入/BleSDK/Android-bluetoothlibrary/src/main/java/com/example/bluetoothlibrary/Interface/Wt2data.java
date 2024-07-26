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
     * ������������ݽ����Ľӿ�
     * @param datas
     * @param mBLE
     * @param activity
     *
     */
    void calculateData_WT2(byte[] datas, BluetoothLeClass mBLE, Activity activity);

    /**
     *  ��������У׼�豸ʱ�䡣����
     * @param mBLE
     */
    void SendForTime(BluetoothLeClass mBLE);


    /**
     * ����յ���3���յ��ͳ���,û���յ��ͼ�����
     * @param mBLE
     */
    void MyTimeTask(final BluetoothLeClass mBLE);

    /**
     * ���������ȡ���¼����ݴ洢״̬
     * @param mBLE
     */



    /**
     * ��������
     * @param
     */
    void setOnWt2DataListener(ResolveWt2.OnWt2DataListener listener);
}
