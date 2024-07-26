package com.example.bluetoothlibrary.Impl;

import android.app.Activity;
import android.util.Log;

import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.Config;

import com.example.bluetoothlibrary.HomeUtil;
import com.example.bluetoothlibrary.Interface.Wt2data;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by laiyiwen on 2017/4/28.
 */

public  class ResolveWt2 implements Wt2data {



    public static int TEMPSTATE = 0;
    private List<Byte> bytes = new ArrayList<Byte>();
    // 1:设备已断开。2:设备已连接.3:搜索设备中... 4:设备不支持蓝牙4.0。
    public static boolean isHadTrimmedValue = false;
    public static int BTBattery = 4;// 电池0-3等级
    public static String tempVersion = ""; //温度计版本
    //wt2的参数
    /**
     * 连接的后第一次接收到设备的状态包。
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
    public boolean IsFirstPack = true;
    public static boolean IsSyncIng = false;
    /**
     * 记录数据包的测量时间。。
     */
    private String BackTime;

    public String time_sycn,ver;
    public int bettray;
    public Double unblanceTemp,balancetemp=0.0;

    private OnWt2DataListener onWt2DataListener ;
    public interface OnWt2DataListener{

        void setUnbalanceTemp(Double unbalanceTemp);
        void setBanlaceTemp(Double banlaceTemp, int btBattery);
        void setWt2ver(String wt2ver);
        void onsycnResult(String time);

    }
    public void setOnWt2DataListener(OnWt2DataListener l){
        onWt2DataListener = l;
    }





    @Override
    public void calculateData_WT2(byte[] datas, BluetoothLeClass mBLE, Activity activity) {
        // 蓝牙连接建立后，App 接收到 体温计的状态包，发现体温计报上来的时间与App本机的时间差超过1分钟时，App
        // 需要发送此命令给体温计来同步时间。

        // 读取体温计中的历史数据。。。。
        // 校对时间。。。。如果设备时间跟app时间相差超过一分钟，则校正设备时间。

        // 然后查询数据总量。。循环发送。。数据块ID从0开始 体温计已发送的数据，不会再保存。。

        if (datas != null) {
            for (int i = 0; i < datas.length; i++) {
                bytes.add(datas[i]);
            }
            int length = bytes.size();
            for (int j = 0; j < bytes.size(); j++) {
                /**
                 * 包头判断方法：包头固定为0xAA，其它可能出现0xAA的地方为：数据段、校验Checksum，找出区别
                 * 1.包头为0xAA时，紧跟一个非0xAA 2.数据为0xAA时，紧跟一个0xAA
                 * 3.校验为0xAA时，紧跟下一个包头0xAA，或者已到包尾
                 */
                if (bytes.get(j) == -86 && j < length - 1
                        && bytes.get(j + 1) != -86) {
                    int n = bytes.get(j + 1); // 总长度，数据中连续的0xAA，计一个长度，总长度包括校验和字节
                    int sum = n; // 累加和，包头和Checksum字节间所有数据的累加，包括总长度字节
                    // 如果数据不完整，结束循环。
                    if (j + 1 + n > length) {
                        break;
                    }
                    // 找到校验位，同时计算累加和
                    for (int k = 0; k < n - 1; k++) {
                        // 如果数据不完整，结束循环。
                        if (j + 1 + n > length) {
                            break;
                        }
                        // 数据中存在0xAA，跳过下一个0xAA，并且n+1
                        if (bytes.get(j + 1 + k + 1) == -86) {
                            if (bytes.get(j + 1 + k + 2) == -86) {
                                k++;
                                n++;
                            } else {
                                // 数据异常----
//                                Log.d("ll", "数据异常,数据中存在不连续的0xAA");
                                // bytes.clear();
                                return;
                            }
                        }
                        int add = bytes.get(j + 1 + k + 1);
                        sum = sum + add;
                    }
                    // 如果数据不完整，结束循环。
                    if (j + 1 + n > length) {
                        break;
                    }
                    // 获取校验和字节
                    int checksum = 0;
                    try {
                        checksum = bytes.get(j + 1 + n); // 此时的n为包括了多余0xAA的实际长度
                    } catch (Exception e) {

                    }
                    // 校验
                    if (checksum != sum % 256) {
                        // 校验失败，数据异常
                        if ((checksum + 256) != sum % 256)
                            continue;
                    }

                    List<Byte> bytesTwo = new ArrayList<Byte>();
                    for (int m = 0; m < n - 1; m++) {
                        bytesTwo.add(bytes.get(j + 1 + m + 1));
                        if (bytes.get(j + 1 + m + 1) == -86
                                && bytes.get(j + 1 + m + 2) == -86) {
                            m++;
                        }
                    }
                    int pID = 0;
                    try {
                        pID = bytesTwo.get(0);
                    } catch (Exception e) {

                    }
                    switch (pID) {
                        case 1:
                            bytes.clear();
                            break;
                        case 2:
                            bytes.clear();
                            break;
                        case 3:
                            // 时间校验， 请求开始发送数据、请求数据重发、结束发送数据 这4种情况下 个App
                            // 下发给体温计的命令，均需要体温计发送ID为0x03
                            // 的响应数据包，App 3s内未收到响应数据包，应继续下发命令给体温计。App
                            // 接收到响应数据包后继续后续的处理逻辑
//                            Log.d("ll", "ID：：0x03：：蓝牙响应数据包：：只有个包头，其他的没有数据的");
                            // 接下来查询设备数据总量。0x67（103） 查询体温计数据存储情况
                            switch (ResponseID) {
                                case 0:
                                    // 接收到检验时间的响应包。
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
                            break;
                        case 17:
                            if ((byte) ((bytesTwo.get(1) >> 0) & 0x1)
                                    + (byte) ((bytesTwo.get(1) >> 1) & 0x1) == 0) {
                                TEMPSTATE = 0;
                                int tempH = bytesTwo.get(3);
                                int tempL = bytesTwo.get(2);
                                if (tempH < 0) {
                                    tempH += 256;
                                } else {
                                }
                                if (tempL < 0) {
                                    tempL += 256;
                                } else {
                                }
                                double temp = ((tempH * 256 + tempL) * 0.01);//获取体温值
//                                Message msg = new Message();
//                                msg.obj = temp;
//                                Log.d("test", "bytesTwo.get(1)::" + bytesTwo.get(1));
//                                Log.d(TAG, "msg.obj::" + msg.obj);
                                if ((byte) ((bytesTwo.get(1) >> 2) & 0x1)
                                        + (byte) ((bytesTwo.get(1) >> 3) & 0x1) == 0) {
                                    Log.d("kk", "unbalance：：");
//                                    msg.what = MACRO_CODE_6;
//                                    isHadTrimmedValue = false;
                                    unblanceTemp=temp;
//                                    onWt2DataListener.setUnbalanceTemp(temp);

                                } else if ((byte) ((bytesTwo.get(1) >> 2) & 0x1)
                                        + (byte) ((bytesTwo.get(1) >> 3) & 0x1) == 1) {
                                    Log.d("kk", "balance：：");
//                                    if (!isHadTrimmedValue) {
                                        //没有经过平衡值, 达到平衡之后就提醒
//                                        msg.what = MACRO_CODE_7;
                                        balancetemp=temp;
//                                        onWt2DataListener.setBanlaceTemp(temp);
//                                        isHadTrimmedValue = true;
//                                    } else {

//                                        onWt2DataListener.setUnbalanceTemp(temp);;
//                                        msg.what = MACRO_CODE_6;
//                                    }
                                } else {

                                }
//                                config.getMyFragmentHandler().sendMessage(msg);
                                // config.sendhideTabmsg(msg);
                                // if( handler1!=null){
                                // handler1.sendMessage(msg);
                                // }
                                // } else if (bytesTwo.get(1) == 1) {
                            } else if ((byte) ((bytesTwo.get(1) >> 0) & 0x1)
                                    + (byte) ((bytesTwo.get(1) >> 1) & 0x1) == 1) {
                                TEMPSTATE = 1;
//                                Message msg = new Message();
//                                msg.what = MainActivity.SENDTEMPHIGHT;
//                                config.getFirstMainHandler().sendMessage(msg);
                                // } else if (bytesTwo.get(1) == 2) {
                            } else if ((byte) ((bytesTwo.get(1) >> 0) & 0x1)
                                    + (byte) ((bytesTwo.get(1) >> 1) & 0x1) == 2) {
                                TEMPSTATE = 2;
                            }
                            bytes.clear();
                            onWt2DataListener.setUnbalanceTemp(unblanceTemp);

                            break;
                        case 18:

                            // Byte3-Byte6是设备的当前时间。需要校对app与设备之间的时间。。如过相差超过60秒，则发送命令调整设备的时间。。
//                            Log.d("test", "接收到蓝牙的状态包：：");
                            if (isFirstStatusPacket) {
                                isFirstStatusPacket = false;
                                int three = bytesTwo.get(3) < 0 ? bytesTwo.get(3) + 256
                                        : bytesTwo.get(3);
                                int four = bytesTwo.get(4) < 0 ? bytesTwo.get(4) + 256
                                        : bytesTwo.get(4);
                                int five = bytesTwo.get(5) < 0 ? bytesTwo.get(5) + 256
                                        : bytesTwo.get(5);
                                int six = bytesTwo.get(6) < 0 ? bytesTwo.get(6) + 256
                                        : bytesTwo.get(6);
                                /**
                                 * 这里返回一个当前时间
                                 */
                                int[] times={three,four,five,six};
                               String time=HomeUtil.BuleToTime(times);
                                if (Math.abs(HomeUtil.DifferTime(six * 256 * 256
                                        * 256 + five * 256 * 256 + four * 256
                                        + three)) <= 60) {
                                    // app时间与设备时间相差小于60秒，无需处理。
                                    // 接下来查询设备数据总量。0x67（103） 查询体温计数据存储情况
//                                    SendForAll(mBLE);
                                    Log.d("buyong","no need to sync"+""+time);
                                    time_sycn=time;
//                                    onWt2DataListener.onsycnResult(time);
                                } else {
                                    SendForTime(mBLE);
                                }
                            }
                            int BTBatteryCopy = bytesTwo.get(1);
                            if (BTBatteryCopy < 0) {
                                BTBattery = (bytesTwo.get(1) + 256) % 16;
                            } else {
                                BTBattery = bytesTwo.get(1) % 16;
                            }
                            bettray=BTBattery;
//                            onWt2DataListener.setBTBattery(BTBattery);
                            Log.v("tkz", "in the bluetooth state the BTBattery ：：" + BTBattery);
                            int version = bytesTwo.get(2);
                            tempVersion = Integer.toString((version / 16)) + "."
                                    + Integer.toString((version % 16));
                            ver=tempVersion;
//                            onWt2DataListener.setWt2ver(tempVersion);
                            bytes.clear();
                            break;
                    }
                    Log.d("time_sycn::",""+time_sycn+"bettray::"+bettray+"ver::"+ver+"unblanceTemp::"+unblanceTemp+"balancetemp::"+balancetemp);
                    onWt2DataListener.onsycnResult(time_sycn);
                    onWt2DataListener.setWt2ver(ver);
                    onWt2DataListener.setUnbalanceTemp(unblanceTemp);
                    onWt2DataListener.setBanlaceTemp(balancetemp,bettray);
//                    onWt2DataListener.setUnbalanceTemp(unblanceTemp);
                }
            }
        }


    }


//    /**
//     * 发送命令校准设备时间。。。
//     */
    public void SendForTime(BluetoothLeClass mBLE) {
        Log.d("kk", "发送命令校准设备时间。。。::");
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

//    /**
//     * 3秒后启动 判断是否接收到校准时间的响应包，如果收到了，则去掉任务，否则继续发送校准时间的命令。
//     */
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





}