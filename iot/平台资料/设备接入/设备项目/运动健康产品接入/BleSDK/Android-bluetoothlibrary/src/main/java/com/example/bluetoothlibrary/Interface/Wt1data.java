package com.example.bluetoothlibrary.Interface;

import android.app.Activity;

import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.Config;
import com.example.bluetoothlibrary.Impl.ResolveWt1;

/**
 * Created by laiyiwen on 2017/4/26.
 */

public interface Wt1data {

      void calculateData_WT1(byte[] datas, BluetoothLeClass mBLE, Activity activity);
      void setOnWt1DataListener(ResolveWt1.OnWt1DataListener listener);
      /**
       * 发送结束发送数据命令。 0代表发送成功结束 1 代表 发送异常结束。
       * @param num
       * @param activity
       * @param mBLE
       */
      void MySendSyncEnd(int num, Activity activity, BluetoothLeClass mBLE);

      /**
       * 15秒后启动 判断是否接收到同步数据开始状态包，如果收到了，则去掉任务，否则继续发送命令请求数据块。
       * @param witch
       * @param mBLE
       */
      void MyReSendPackTask(int witch, final BluetoothLeClass mBLE);


      /**
       * 请求数据重发。
       * @param num
       * @param mBLE
       */
      void SendRepeatRequest(int num, BluetoothLeClass mBLE);


      /**
       * 请求开始发送数据。
       * @param num
       * @param mBLE
       */

      void SendRequestForDate(int num, BluetoothLeClass mBLE);


      /**
       *  5秒后启动 判断是否接收到同步数据开始状态包，如果收到了，则去掉任务，否则继续发送命令请求数据块。
       * @param witch
       * @param mBLE
       */

      void MyReSendTask(int witch, final BluetoothLeClass mBLE);

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
      void SendForAll(BluetoothLeClass mBLE);

}
