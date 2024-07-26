package com.example.bluetoothlibrary.Impl;

import android.app.Activity;
import android.util.Log;

import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.Config;

import com.example.bluetoothlibrary.HomeUtil;
import com.example.bluetoothlibrary.Interface.Wt1data;
import com.example.bluetoothlibrary.MyDateUtil;
import com.example.bluetoothlibrary.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by laiyiwen on 2017/4/26.
 */

/**
 * wt1  数据解析的回调接口
 */

public class ResolveWt1 implements Wt1data {



    private List<Byte> bytes = new ArrayList<Byte>();
    // 1:设备已断开。2:设备已连接.3:搜索设备中... 4:设备不支持蓝牙4.0。
    public static int BTBattery = 4;// 电池0-3等级
    public static String tempVersion = ""; //温度计版本
    String time;
    public static int TEMPSTATE = 0;
    public static final int MACRO_CODE_3 = 3;
    public static final int MACRO_CODE_4 = 4;
    public static final int MACRO_CODE_5 = 5;
/** 连接的后第一次接收到设备的状态包。
            */
    public boolean isFirstStatusPacket = true;
    /**
     * 接收到0x03包头的数据 标记是那个操作的 0：校验时间 1：请求开始发送数据 2： 请求重发数据 3：结束发送数据。。。
     * 4:请求设备存储的数据总量。
     */
    public int ResponseID = 0;
    /**
     * 是否接收到校验时间的响应包。
     */
    public boolean isCheckTime = false;
    /**
     * 设备中一共有多少个需要同步的数据块。
     */
    public int DataBlock = 0;
    /**
     * 已经请求到哪个数据块了。
     */
    public int WitchDateBlock = 0;
    /**
     * 标记是否是第一个数据包。。。
     */
    public int WitchPack = 0;
    public int total = 0,begin=0;
    public boolean IsFirstPack = true;
    public static boolean IsSyncIng = false;

    public static int firstIn=0;
    /**
     * 记录数据包的测量时间。。
     */
    double temp;
    private String BackTime;

    private OnWt1DataListener onWt1DataListener ;
    public interface OnWt1DataListener {
        void setTemp(Double temp);
         void ontempState(int stateCode);
        void onBTBattery(String bTBattery);
        void onVersion(String version);
        void onTime(String time);
        void ontotal(int total);
        void onsycnResult(float BlueTem, String TempID);
        void onSycnState(int begin, int total, String backtime);

    }

    public void setOnWt1DataListener(OnWt1DataListener l){
        onWt1DataListener = l;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }
    @Override
    public synchronized void calculateData_WT1(byte[] datas,BluetoothLeClass mBLE, Activity activity)  {
        if (datas != null) {
            for (int i = 0; i < datas.length; i++) {
                bytes.add(datas[i]);
            }
            Log.d("length",""+bytes.size());

            int length = bytes.size();
            for (int j = 0; j < bytes.size(); j++) {
                /**
                 * 包头判断方法：包头固定为0xAA，其它可能出现0xAA的地方为：数据段、校验Checksum，找出区别
                 * 1.包头为0xAA时，紧跟一个非0xAA 2.数据为0xAA时，紧跟一个0xAA
                 * 3.校验为0xAA时，紧跟下一个包头0xAA，或者已到包尾
                 */
                if (bytes.get(j) == -86 && j < length - 1 && bytes.get(j + 1) != -86) {

                    int n =byteToInt(bytes.get(j + 1)) ; // 获得数据总长度n，数据中连续的0xAA，计一个长度，总长度包括校验和字节
                    int sum = n; // 累加和，包头和Checksum字节间所有数据的累加，包括总长度字节
                    // 如果数据不完整，结束循环。


                    if (j + 1 + n > length) {

                        break;
                    }
                    // 找到校验位，同时计算累加和sum
                    for (int k = 0; k < n - 1; k++) {
                        // 如果数据不完整，结束循环。
                        if (j + 1 + n > length) {

                            break;
                        }
                        // 数据中存在0xAA，跳过下一个0xAA，并且n+1(因为获得的字节长度不含有0xaa)
                        if (bytes.get(j + 1 + k + 1) == -86) {
                            if (bytes.get(j + 1 + k + 2) == -86) {
                                k++;
                                n++;
                            } else {
                                // 数据异常----
//                                Log.d("test", "数据异常,数据中存在不连续的0xAA");
                                // bytes.clear();
                                return;
                            }
                        }
                        int add = byteToInt(bytes.get(j + 1 + k + 1));
                        sum = sum + add;//包头和Checksum字节间所有数据的累加，包括总长度字节
                        sum=sum%256;
                    }
                    // 如果数据不完整，结束循环。
                    if (j + 1 + n > length) {

                        break;
                    }
////                     获取校验和字节
                    int checksum = byteToInt(bytes.get(j + 1 + n));// 此时的n为包括了多余0xAA的实际长度
//                  Log.d("checksum----",""+checksum+"j="+j+",---n="+n+"sum="+sum+",sum % 256="+sum % 256);
//                    for (int i=0;i<bytes.size();i++)
//                    {
//                        Log.d("?","第"+i+"等于"+bytes.get(i).intValue());
//                    }
//            //   校验
//
                    if (checksum != sum % 256) {
                        // 校验失败，数据异常

                        if ((checksum + 256) != sum % 256)
                            continue;
                    }

                    List<Byte> bytesTwo = new ArrayList<Byte>();
                    //提取数据块,去掉多余0xaa
                    for (int m = 0; m < n - 1; m++) {
                        bytesTwo.add(bytes.get(j + 1 + m + 1));
                        if (bytes.get(j + 1 + m + 1) == -86 && bytes.get(j + 1 + m + 2) == -86) {
                            m++;
                        }
                    }

//                    for (int i=0;i<bytesTwo.size();i++)
//                    {
//                        Log.d("现在出现的bytesTwo是什么?","第"+i+"等于"+bytesTwo.get(i).intValue());
//                    }
//                    for (Byte b:bytesTwo)
//                    {
//                        Log.d("",""+b.intValue());
//                    }


                    int pID = bytesTwo.get(0);
                    Log.d("test", "pID =" + pID);

                    switch (pID) {
                        case 1://旧体温数据
                            bytes.clear();//清空数组,防止数据叠加
                            break;
                        case 2://旧体温计状态
                            bytes.clear();
                            break;
                        case 3:
                            // 接收到响应数据包后继续后续的处理逻辑
//                            Log.d("kkk", "ID：：0x03：：蓝牙响应数据包：：只有个包头，其他的没有数据的");
                            // 接下来查询设备数据总量。0x67（103） 查询体温计数据存储情况
                            switch (ResponseID) {
                                case 0:

//                                    SendForAll(mBLE);
                                    isCheckTime = true;
                                    break;
                                case 1:
                                    // 接收到请求发送数据的响应包。
//                                    Log.d("test", " 接收到请求发送数据的响应包::响应包：：");
                                    break;
                                case 2:
                                    // 接收到请求重发数据的响应包。。
//                                    Log.d("test", "接收到请求重发数据的响应包::响应包：：");
                                    break;
                                case 3:
                                    // 接收到结束发送数据的响应包。。
//                                    Log.d("test", " 接收到结束发送数据的响应包::响应包：：");
                                    break;
                                case 4:
                                    // 请求设备的数据存储总量。
//                                    Log.d("test", "请求设备的数据存储总量::响应包：：");
                                    break;
                                default:
                                    break;
                            }
                            bytes.clear();
                            break;
                        case 17://体温数据0x11(新协议)
                            if (bytesTwo.get(1) == 0) {//正常温度
                                TEMPSTATE = 0;
                                int tempH = bytesTwo.get(3);//体温高8位
                                int tempL = bytesTwo.get(2);//体温低8位
                                if (tempH < 0) {
                                    tempH += 256;
                                }
                                if (tempL < 0) {
                                    tempL += 256;
                                }
                                 temp = ((tempH * 256 + tempL) * 0.01);//计算温度
                                Log.d("test", "case 17::tempH =" + tempH);
                                Log.d("test", "case 17::tempL =" + tempL);
                                Log.d("test", "case 17::temp =" + temp);
//                                Log.d("看看这里", "111111111");


//                                Message msg = new Message();
//                                msg.what = MACRO_CODE_3;//温度标记1003,发送到FirstMainActivity中的FirstMainHandler中处理
//                                msg.obj = temp;//将解码后的温度传到FirstMainActivity中得线程中处理
//                                config.getMyFragmentHandler().sendMessage(msg);//往主线程发送message
                            } else if (bytesTwo.get(1) == 1) {//温度过高(>50)
                                Log.i("test", "SENDTEMPHIGHT");
                                TEMPSTATE = 1;

//                                onWt1DataListener.ontempState(MACRO_CODE_4);
//                                Message msg = new Message();
//                                msg.what = MACRO_CODE_4;
//                                config.getMyFragmentHandler().sendMessage(msg);//显示温度过高
//
                            } else if (bytesTwo.get(1) == 2) {//温度过低(<0)
//                                onWt1DataListener.ontempState(MACRO_CODE_5);
//                                Message msg = new Message();
//                                msg.what = MACRO_CODE_5;
//                                config.getMyFragmentHandler().sendMessage(msg);//显示温度过低
//                                Log.d("test", "SENDTEMPLOW");
                                TEMPSTATE = 2;
                            }
                            bytes.clear();
                            break;
                        case 18://体温计状态0x12(新协议)


                                int BTBatteryCopy = bytesTwo.get(1);
                                if (BTBatteryCopy < 0) {
                                    BTBattery = (bytesTwo.get(1) + 256) % 16;//求出蓝牙的电量,对16求余,取出其低4位
                                } else {
                                    BTBattery = bytesTwo.get(1) % 16;
                                }

//                            onWt1DataListener.onBTBattery(""+BTBattery);

                                int version = bytesTwo.get(2);//蓝牙版本号X.Y,高4位位X,低4位为Y
                                tempVersion = Integer.toString((version / 16)) + "."
                                        + Integer.toString((version % 16));

//                            onWt1DataListener.onVersion(""+version);
                                int three = bytesTwo.get(3) < 0 ? bytesTwo.get(3) + 256
                                        : bytesTwo.get(3);
                                int four = bytesTwo.get(4) < 0 ? bytesTwo.get(4) + 256
                                        : bytesTwo.get(4);
                                int five = bytesTwo.get(5) < 0 ? bytesTwo.get(5) + 256
                                        : bytesTwo.get(5);
                                int six = bytesTwo.get(6) < 0 ? bytesTwo.get(6) + 256
                                        : bytesTwo.get(6);
                            bytes.clear();
                                int[] times = {three, four, five, six};
                                 time = HomeUtil.BuleToTime(times);

//
                            break;
                        case 20:
//                            Log.d("IsFirstPack", "" + IsFirstPack);
//                            if (IsFirstPack) {
//                                IsFirstPack = false;
                            WitchDateBlock=bytesTwo.get(1)+bytesTwo.get(2)*256;
                            WitchPack=bytesTwo.get(3)+bytesTwo.get(4)*256;
//                            if (WitchDateBlock<DataBlock)//如果小于总快数有
//                            {

                                    for (n=5;n<bytesTwo.size();n=n+2)
                                    {
                                        float BlueTem = (float) ((((bytesTwo.get(n) < 0 ? bytesTwo
                                                .get(n) + 256 : bytesTwo.get(n)) + (bytesTwo
                                                .get(n+1) < 0 ? bytesTwo.get(n+1) + 256
                                                : bytesTwo.get(n+1)) * 256)) * 0.01);
//                                        Log.d("test", "第" + n + "个数据的温度：：" + BlueTem);
                                    }

//                                if (WitchPack==total-1)
//                                {
//
////
//                                        bytes.clear();
//                                        WitchDateBlock++;
//                                        SendRequestForDate(WitchDateBlock,mBLE);
////
//                                }
//                            }

                                // HomeUtil.DegreesToFahrenheit
                                // 根据测量时间转换成的TempID查找数据库，如果存在记录，则判断测量时间的前后，保留时间早的数据。
                                //判断是不是客户模式如果是客户模式就什么都不干

                                String TempID = HomeUtil.Date2ID(BackTime);
                                Log.d("返回的时间",""+TempID);

                                bytes.clear();
//                                if (WitchDateBlock < DataBlock) {
//                                    SendRequestForDate(WitchDateBlock,mBLE);
//                                    MyReSendPackTask(WitchDateBlock,mBLE);
//                                    WitchDateBlock++;
//                                } else {
//                                    Log.d("test", "设备的数据同步完毕：：：可以发送：：结束数据发送了：：");
//                                    MySendSyncEnd(0,activity,mBLE);
//                                }
//
////                                onWt1DataListener.onsycnResult(BlueTem,TempID);
//                            }

//                            bytes.clear();
//                            onWt1DataListener.onsycnResult(BlueTem,TempID);

//                            bytes.clear();
                            break;
                        case 21:
//                            IsFirstPack = true;
//                            Log.d("test", "同步数据开始状态包：：");
//                            Log.d("test", "开始状态包：：该数据块的编号：："
//                                    + (bytesTwo.get(1) + bytesTwo.get(2) * 256));
                             begin=bytesTwo.get(1) + bytesTwo.get(2) * 256;
//                            Log.d("test", "开始状态包：：该数据块的数据包总量：："
//                                    + (bytesTwo.get(7) + bytesTwo.get(8) * 256));
                             total=bytesTwo.get(7) + bytesTwo.get(8) * 256;
                            BackTime = HomeUtil.BuleToTime(new int[]{
                                    bytesTwo.get(3), bytesTwo.get(4),
                                    bytesTwo.get(5), bytesTwo.get(6)});
//                            Log.d("test", "开始状态包：：该数据库的时间：：" + BackTime);
                            bytes.clear();
//                            bytes.clear();
//                            onWt1DataListener.onSycnState(begin,total,BackTime);
//                            MyReSendPackTask(WitchDateBlock,mBLE);
//                            WitchDateBlock++;
//                            WitchDateBlock++;
//                            bytes.clear();
                            break;
                        case 22:
                            int all = bytesTwo.get(1) + bytesTwo.get(2) * 256;
                            bytes.clear();
//                            onWt1DataListener.ontotal(all);
//                            Log.d("test", "ID：：0x16 体温计数据存储包:数据块一共有多少个::" + all);
                            if (all > 0) {
                                // 开始请求设备发送需要同步的数据。
                                DataBlock = all;
                                SendRequestForDate(WitchDateBlock,mBLE);
                            } else {
                                DataBlock = 0;
                                IsSyncIng = false;
                                // 记录本次同步时间。。
//                                Log.d("test", "需要同步的数据为0：：");
                                SharedPreferencesUtil
                                        .setEquipmentSynchronizationTime(
                                                activity,
                                                MyDateUtil.getDateFormatToString("yyyy-MM-dd HH:mm"));
                            }
                            bytes.clear();
//                            onWt1DataListener.ontotal(all);
//                            bytes.clear();
                            break;

                    }



                }
                onWt1DataListener.setTemp(temp);
                if (TEMPSTATE==1)
                {
                    onWt1DataListener.ontempState(MACRO_CODE_4);
                }
                if (TEMPSTATE==2)
                {
                    onWt1DataListener.ontempState(MACRO_CODE_5);
                }
                onWt1DataListener.onBTBattery("" + BTBattery);
                onWt1DataListener.onVersion("" + tempVersion);
                onWt1DataListener.onTime(time);

            }


        }
    }


    /**
     *  发送结束发送数据命令。 0代表发送成功结束 1 代表 发送异常结束
     * @param num
     * @param activity
     * @param mBLE
     */


    public void MySendSyncEnd(int num, Activity activity, BluetoothLeClass mBLE) {

        List<Byte> sendforAll = new ArrayList<Byte>();
        sendforAll.add((byte) 0xAA);// 包头。固定为0xAA。 -86。。。
        sendforAll.add((byte) 0x04);// 长度 数据块的长度
        // 数据块
        sendforAll.add((byte) 0x6A);

        sendforAll.add((byte) num);// 0
        sendforAll.add((byte) 0x00);// 保留
        // 校验
        int CRS = 0;
        for (int i = 2; i < sendforAll.size(); i++) {
            CRS += sendforAll.get(i);
        }
        sendforAll.add((byte) CRS);
//        Log.d("test", "发送命令请求结束发送数据。::" + num);
        if (mBLE != null) {
            mBLE.writeCharacteristic(HomeUtil
                    .CheckByte(sendforAll));
        }
        ResponseID = 3;
        IsSyncIng = false;
        SharedPreferencesUtil.setEquipmentSynchronizationTime(activity,
                MyDateUtil.getDateFormatToString("yyyy-MM-dd HH:mm"));
    }

    /**
     * 15秒后启动 判断是否接收到同步数据开始状态包，如果收到了，则去掉任务，否则继续发送命令请求数据块。
     */

    public void MyReSendPackTask(int witch, final BluetoothLeClass mBLE) {
        final Timer timer = new Timer();
        final int num = witch;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (num == WitchDateBlock - 1 && IsFirstPack) {
                    // 重新请求该数据块的数据。
                    SendRepeatRequest(WitchDateBlock,mBLE);
                } else {
                    timer.cancel();
                }
            }
        };
        timer.schedule(task, 15000);
    }

    /**
     * 请求数据重发。。
     *
     * @param num
     */
    public void SendRepeatRequest(int num,BluetoothLeClass mBLE) {

        List<Byte> sendforAll = new ArrayList<Byte>();
        sendforAll.add((byte) 0xAA);// 包头。固定为0xAA。 -86。。。
        sendforAll.add((byte) 0x07);// 长度 数据块的长度
        // 数据块
        sendforAll.add((byte) 0x69);
        // byte2 byte3是数据块编号 从0开始。。
        int[] ID = HomeUtil.getDateID(num);
        sendforAll.add((byte) ID[0]);
        sendforAll.add((byte) ID[1]);
        sendforAll.add((byte) 0x00);// 0
        sendforAll.add((byte) 0x00);// 0
        sendforAll.add((byte) 0x00);// 保留
        // 校验
        int CRS = 0;
        for (int i = 2; i < sendforAll.size(); i++) {
            CRS += sendforAll.get(i);
        }
        sendforAll.add((byte) CRS);
//        Log.d("test", "发送命令重新请求发送数据块num的数据包。::" + num);
        if (mBLE != null) {
            mBLE.writeCharacteristic(HomeUtil
                    .CheckByte(sendforAll));
        }
        ResponseID = 2;
    }

    /**
     * 请求开始发送数据。
     *
     * @param num
     */
    public void SendRequestForDate(int num,BluetoothLeClass mBLE) {

        List<Byte> sendforAll = new ArrayList<Byte>();
        sendforAll.add((byte) 0xAA);// 包头。固定为0xAA。 -86。。。
        sendforAll.add((byte) 0x05);// 长度 数据块的长度
        // 数据块
        sendforAll.add((byte) 0x68);
        sendforAll.add((byte) 0x00);// 保留
        // byte2 byte3是数据块编号 从0开始。。
        int[] ID = HomeUtil.getDateID(num);
        sendforAll.add((byte) ID[0]);
        sendforAll.add((byte) ID[1]);
        // 校验
        int CRS = 0;
        for (int i = 2; i < sendforAll.size(); i++) {
            CRS += sendforAll.get(i);
        }
        sendforAll.add((byte) CRS);
//        Log.d("test", "发送命令获取体温计存储的数据块::" + num);
        if (mBLE != null) {
            mBLE.writeCharacteristic(HomeUtil
                    .CheckByte(sendforAll));
            ResponseID = 1;
            // 这里发送了，如果5秒后没有收到同步数据开始状态包，则需要请求数据重发。。
            // 开启任务。
//            MyReSendTask(num,mBLE);
        }
    }

    /**
     * 5秒后启动 判断是否接收到同步数据开始状态包，如果收到了，则去掉任务，否则继续发送命令请求数据块。
     */
    public void MyReSendTask(int witch, final BluetoothLeClass mBLE) {
//        Log.d("", "发送命令请求数据块::");
        final Timer timer = new Timer();
        final int num = witch;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (num == WitchDateBlock) {
                    // 重新请求该数据块的数据。
                    SendRequestForDate(WitchDateBlock,mBLE);
                } else {
                    timer.cancel();
                }
            }
        };
        timer.schedule(task, 5000);
    }

    /**
     * 发送命令校准设备时间。。。
     */
    public void SendForTime(BluetoothLeClass mBLE) {
//        Log.d("", "发送命令校准设备时间。。。::");
        int[] nowTime = HomeUtil.getTimeByte();
        // app时间与设备时间相差大于60秒，校准设备时间。byte2-byte5当前时间 byte
        // 6保留
        // byte0 包头 byte1保留
        // 包头（0xAA） 长度 数据块 校验Checksum
        // 包头：固定为0xAA，
        // 长度：为该字节之后所有数据的长度，包括校验和字节，此字节不能为0xAA。
        // 数据块：打包的具体生理数据。
        // Checksum：校验字节，为包头和Checksum字节间所有数据的累加校验和。
        List<Byte> sendbytes = new ArrayList<Byte>();
        sendbytes.add((byte) 0xAA);// 包头。固定为0xAA。
        // //170。。。
        sendbytes.add((byte) 0x08);// 长度 数据块的长度
        // 。。7byte+1byte校验
        // 以下是数据块。
        sendbytes.add((byte) 0x6B);// beye0
        sendbytes.add((byte) 0x00);// beye1保留
        // byte2-byte5当前时间。
        sendbytes.add((byte) nowTime[0]);// beye2
        sendbytes.add((byte) nowTime[1]);// beye3
        sendbytes.add((byte) nowTime[2]);// beye4
        sendbytes.add((byte) nowTime[3]);// beye5
        // 6 保留
        sendbytes.add((byte) 0x00);// beye6
        // 最后是校验位。
        int size = sendbytes.size();
        // crc校验
        byte crc = 0x00;
        for (int i = 0; i < size; i++) {
            crc += sendbytes.get(i);
        }
        sendbytes.add(crc);
        if (mBLE != null) {
            mBLE.writeCharacteristic(HomeUtil
                    .CheckByte(sendbytes));
            ResponseID = 0;
            isCheckTime = false;
            // 启动线程 ，3秒后如果isCheckTime不为true的话，重新发送命令校验时间。。
            MyTimeTask(mBLE);
        }
    }

    /**
     * 3秒后启动 判断是否接收到校准时间的响应包，如果收到了，则去掉任务，否则继续发送校准时间的命令。
     */
    public void MyTimeTask(final BluetoothLeClass mBLE) {
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isCheckTime) {
                    timer.cancel();
                } else {
                    SendForTime(mBLE);
                }
            }
        };
        timer.schedule(task, 3000);
    }

    /**
     * 发送命令获取体温计数据存储状态
     */
      public void SendForAll(BluetoothLeClass mBLE) {
//        Log.d("show", "发送命令获取体温计数据存储状态::");
        IsSyncIng = true;
        ResponseID = 4;
        List<Byte> sendforAll = new ArrayList<Byte>();
        sendforAll.add((byte) 0xAA);// 包头。固定为0xAA。 //170。。。
        sendforAll.add((byte) 0x03);// 长度 数据块的长度
        // 数据块
        sendforAll.add((byte) 0x67);
        sendforAll.add((byte) 0x00);
        // 校验
        sendforAll.add((byte) 0x67);
//        Log.d("test", "发送命令获取体温计数据存储状态::");
        if (mBLE != null) {
            mBLE.writeCharacteristic(HomeUtil
                    .CheckByte(sendforAll));
        }
    }

}
