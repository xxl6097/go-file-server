package com.example.bluetoothlibrary.Impl;

import android.util.Log;

import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.HomeUtil;
import com.example.bluetoothlibrary.Interface.Mf100data;

import static com.example.bluetoothlibrary.StringUtill.hexString2binaryString;

/**
 * Created by laiyiwen on 2017/4/28.
 */

public class ResolveWf100 implements Mf100data {
    /**
     *解析胎监的数据*********************************************************************************************************************************************************
     */
    public static boolean isFirstReceive=true;
    private ResolveWf100.OnWF100DataListener onWF100DataListener ;
    public interface OnWF100DataListener {

        void ontime(String time);
        void onverion(String mainversion, String subversion);
        void onfr1(int Fr1);
        void onquantity(String battery);
        void Onvoice(String voice);



    }

    public void setOnWF100DataListener(ResolveWf100.OnWF100DataListener l){
        onWF100DataListener = l;
    }



    String time,firstversion,subversion,volume;
    int on_fr1;
    String quantity;
    @Override
    public synchronized void resolveBPData_wf(String datas)
    {
        String head = datas.substring(0, 2);
        //处理AAAA
        if (datas.contains("AAAA")) {
            Log.w("TAG", "AAAAAAAAAAAAAAA shock!");
            datas = datas.replaceAll("AAAA", "AA");
        }

        if (head.equals("AA") & datas.length() > 4)
        {
            String dataID = datas.substring(4, 6);

            if (dataID.equals("48"))
            {
                if (isFirstReceive){
                    int heartId=Integer.parseInt(datas.substring(6, 8), 16);
//                    Log.d("现在这个数据的数据包ID",""+heartId);
                    //第二个字节
                    String  heartState=hexString2binaryString(datas.substring(8, 10));
                    if (heartState.substring(0,1).equals("1"))
                    {
//                        Log.d("heartState.substring(0,1)"+heartState.substring(0,1),"双胎"+"");
                    }else{
//                        Log.d("heartState.substring(0,1)"+heartState.substring(0,1),"单胎"+"");
                    }
                    if (heartState.substring(1,2).equals("1"))
                    {
//                        Log.d("heartState.substring(1,2)"+heartState.substring(1,2),"胎心率1脱落"+"");

                    }else
                    {
//                        Log.d("heartState.substring(1,2)"+heartState.substring(1,2),"胎心率1不脱落"+"");
                    }
                    if (heartState.substring(2,3).equals("1"))
                    {
//                        Log.d("heartState.substring(2,3)"+heartState.substring(2,3),"胎心率2脱落"+"");

                    }else
                    {
//                        Log.d("heartState.substring(2,3)"+heartState.substring(2,3),"胎心率2不脱落"+"");
                    }
                    if (heartState.substring(3,4).equals("1"))
                    {
//                        Log.d("heartState.substring(3,4)"+heartState.substring(3,4),"宫缩脱落"+"");

                    }else
                    {
//                        Log.d("heartState.substring(3,4)"+heartState.substring(3,4),"宫缩不脱落"+"");
                    }
                    if (heartState.substring(4,5).equals("1"))
                    {
//                        Log.d("heartState.substring(4,5)"+heartState.substring(4,5),"无重合报警"+"");

                    }else
                    {
//                        Log.d("heartState.substring(4,5)"+heartState.substring(4,5),"胎心重合报警"+"");
                    }
                    String  DeviceState=hexString2binaryString(datas.substring(10, 12));

                    if (DeviceState.substring(0,1).equals(1))
                    {
//                        Log.d("","有胎动");
                    }else{
//                        Log.d("","没胎动");
                    }
                    if (DeviceState.substring(1,4).equals("001"))
                    {
//                        Log.d("","信号差"+DeviceState.substring(1,4));

                    }
                    else if(DeviceState.substring(1,4).equals("010"))
                    {
//                        Log.d("","信号一般"+DeviceState.substring(1,4));
                    }else{
//                        Log.d("信号好","DeviceState.substring(1,4)"+DeviceState.substring(1,4));
                    }
                    isFirstReceive=false;
                }else
                {
                    int temp=0;
                    if (hexString2binaryString(datas.substring(12, 14)).substring(7,8).equals("1"))
                    {
                        temp=1;
                    }else
                    {
                        temp=0;
//                      Log.d("temp",""+temp);
                    }

                    String string_FHR=hexString2binaryString(datas.substring(14,16));
                    string_FHR=string_FHR.substring(1,8);
//                  Log.d("Str1",""+string_FHR);3
                    string_FHR=temp+""+string_FHR;
                  Log.d("Str1",""+string_FHR);
                    int FHR1= Integer.parseInt(string_FHR,2);
                  Log.d("FHR1.....",""+FHR1);
                    on_fr1=FHR1;

                    int temp1=0;
                    if (hexString2binaryString(datas.substring(12, 14)).substring(6,7).equals("1"))
                    {
                        temp1=1;
                    }else
                    {
                        temp1=0;
                    }
                    String string_FHR2=hexString2binaryString(datas.substring(16,18));
                    string_FHR2=string_FHR2.substring(1,8);
                    string_FHR2=temp1+""+string_FHR2;
                    int FHR2= Integer.parseInt(string_FHR2,2);
                  Log.d("FHR2.....",""+FHR2);
                    int temp2=0;
                    if (hexString2binaryString(datas.substring(12, 14)).substring(5,6).equals("1"))
                    {
                        temp2=1;
                    }else
                    {
                        temp2=0;
                    }
                    String UC=hexString2binaryString(datas.substring(18,20));
                    UC=UC.substring(1,8);
                    UC=temp2+""+UC;
                    int UC_int= Integer.parseInt(UC,2);
                  Log.d("uc.....",""+UC_int);
                    int temp3=0;
                    if (hexString2binaryString(datas.substring(12, 14)).substring(4,5).equals("1"))
                    {
                        temp3=1;
                    }else
                    {
                        temp3=0;
                      Log.d("temp3",""+temp3);
                    }
                    String FM=hexString2binaryString(datas.substring(20,22));
                    FM=FM.substring(1,8);
                    FM=temp2+""+FM;
                    int FM_int= Integer.parseInt(FM,2);
                  Log.d("FM_int.....",""+FM_int);

                }
                //第一个字节


            }else{

                if (dataID.equals("49"))
                {

                     quantity=datas.substring(6, 8);
//                    quantity=""+Integer.parseInt(quantity,2);
                     volume=datas.substring(8, 10);
//                    volume=""+Integer.parseInt(volume,2);
                    if (volume.equals("0")){
//                        Log.d("kkk","静音");
                    }else if(volume.equals("1"))
                    {
//                        Log.d("kkkkk","一级");
                    }else if (volume.equals("2"))
                    {
//                        Log.d("","二级");
                    }else if (volume.equals("3")){
//                        Log.d("","三级");
                    }else if (volume.equals("4")){
//                        Log.d("","四级");
                    }else if (volume.equals("5"))
                    {
//                        Log.d("","五级");
                    }
                    String wokeMode=datas.substring(10, 12);
                    wokeMode=Integer.parseInt(wokeMode,2)+"";

                    if (wokeMode.equals("0")){
//                        Log.d("","胎心测量");
                    }else if(wokeMode.equals("1"))
                    {
//                        Log.d("","收音机");
                    }else if (wokeMode.equals("2"))
                    {
//                        Log.d("","音乐播放器");
                    }else if (wokeMode.equals("3")){
//                        Log.d("","蓝牙音箱");
                    }

                    String main_version=hexString2binaryString(datas.substring(12, 14));
                    int mainx= Integer.parseInt(main_version.substring(0,4),2);
                    int mainy= Integer.parseInt(main_version.substring(4,8),2);

                    String second_version=hexString2binaryString(datas.substring(14, 16));
                    int secondy= Integer.parseInt(second_version.substring(0,4),2);
                    int secondx= Integer.parseInt(second_version.substring(4,8),2);

                    String z_version=hexString2binaryString(datas.substring(16, 18));
                    int secondz= Integer.parseInt(z_version.substring(0,4),2);
                    int mainz= Integer.parseInt(z_version.substring(4,8),2);

                    Log.d("main",""+mainx+"."+mainy+"."+mainz);
                    Log.d("second",""+secondx+"."+secondy+"."+secondz);
                    firstversion=""+mainx+"."+mainy+"."+mainz;
                    subversion=""+secondx+"."+secondy+"."+secondz;

                    String time1=hexString2binaryString(datas.substring(18, 20));
                    int  one=Integer.parseInt(time1,16);
                    String time2=hexString2binaryString(datas.substring(20, 22));
                    int  two=Integer.parseInt(time2,16);
                    String time3=hexString2binaryString(datas.substring(22, 24));
                    int  three=Integer.parseInt(time3,16);
                    String time4=hexString2binaryString(datas.substring(24, 26));
                    int  four=Integer.parseInt(time4,16);

                    int[] times={one,two,three,four};
                     time= HomeUtil.BuleToTime(times);
                    Log.d("device time",""+time);

                }

            }
            Log.d("time","::"+time+"firstversion::"+firstversion+"subversion::"+subversion);
            onWF100DataListener.ontime(time);
            onWF100DataListener.onverion(firstversion,subversion);
            onWF100DataListener.onfr1(on_fr1);
            onWF100DataListener.onquantity(quantity);
            onWF100DataListener.Onvoice(volume);

        }




    }

    public void SetVoice(BluetoothLeClass mBLE,byte[] datas)
    {
        mBLE.writeCharacteristic_wbp(datas);
    }

}
