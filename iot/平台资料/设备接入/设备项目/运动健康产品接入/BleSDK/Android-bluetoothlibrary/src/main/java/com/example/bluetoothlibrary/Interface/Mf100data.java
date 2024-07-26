package com.example.bluetoothlibrary.Interface;

import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.Impl.ResolveWf100;

/**
 * Created by laiyiwen on 2017/4/28.
 */

public interface Mf100data  {

    void setOnWF100DataListener(ResolveWf100.OnWF100DataListener l);
    void resolveBPData_wf(String datas);
    void SetVoice(BluetoothLeClass mBLE, byte[] datas);
}
