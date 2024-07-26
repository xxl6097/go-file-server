package com.example.bluetoothlibrary.Impl;

import android.text.StaticLayout;
import android.util.Log;


import com.example.bluetoothlibrary.Interface.M70cdata;

import java.util.Vector;

/**
 * Created by laiyiwen on 2017/4/28.
 */

public class ResolveM70c implements M70cdata {
    /**
     * resolve oxygen blood data
     * ********************************************************************************************************************************
     */
    private String receivedData = "";
    private boolean isFirstReceive = true;  // is the first time receive the boardcast
    private int historyCounter = 0;//history record

    private Vector<Integer> SPO2WaveValues=new Vector<>();
    private Vector<Integer> PIValues=new Vector<>();
    String main,sub,battery;

   public OnM70cDataListener onM70cDataListener;



   public interface OnM70cDataListener{
       void setSPO2Value(int spo2Value);
       void setHeartRateValue(int heartRateValue);
       void setPI(Float pi);
       void setRespValue(int respValue);
       void setbattery(String battery);
       void setMain(String main);
       void setSub(String sub);
       void setSPo2ValuesPIValues(Vector<Integer> sp02WaveValues, Vector<Integer> piValues);

   }


    public void setOnM70cDataListener(OnM70cDataListener listener)
    {
        onM70cDataListener=listener;
    }

    @Override
    public void clear() {
        SPO2WaveValues=new Vector<>();

        PIValues=new Vector<>();
    }


    public synchronized void calculateData_M70c(String rawData) {


        String head = rawData.substring(0, 2);
        String data = rawData.replace(" ","");
        //处理AAAA
        if (data.contains("AAAA")) {
            Log.w("TAG", "AAAAAAAAAAAAAAA shock!");
            data = data.replaceAll("AAAA", "AA");
        }

        if (head.equals("AA") & data.length() > 4) {
            String dataID = data.substring(4, 6);
            if (dataID.equals("41")) {
                Log.d("TAG", "received data values is : " + receivedData);
                historyCounter++;
                if (historyCounter == 10) {
                    historyCounter = 0;
                }
                if (!isFirstReceive) {

                    int SPO2Value = Integer.parseInt(receivedData.substring(72, 74), 16) & 0x7F;//就是为了高位清零
                    Log.d("spo2  valuue", "" +SPO2Value );
                    onM70cDataListener.setSPO2Value(SPO2Value);
//                    if (SPO2Value == 127) {//如果等于127那就说明超出了范围了
//                        SpO2ValueTextView.setText(" -- ");
//                    } else {
//                        SpO2ValueTextView.setText("" + SPO2Value);
//                    }
                    // display heart rate value
                    int heartRateValue = Integer.parseInt(receivedData.substring(74, 76), 16);
                    Log.d("heartRateValue  valuue", "" +heartRateValue);

                    onM70cDataListener.setHeartRateValue(heartRateValue);
                    for (int i = 0; i < 30; i++) {
//                        Log.d("receivedData.substring(10 + i * 2, 12 + i * 2)","第"+i+""+receivedData.substring(10 + i * 2, 12 + i * 2));
                        int PIValue = Integer.parseInt(receivedData.substring(10 + i * 2, 12 + i * 2), 16);
//                        Log.d("PIvaule","第"+i+""+PIValue);
                        PIValues.add(PIValue);
                    }

//
                    float PI = Integer.parseInt(receivedData.substring(70, 72), 16) / 10.0f;
                    Log.d("PI",""+PI);
                    onM70cDataListener.setPI(PI);
//                    if (PI == 25.5) {
//                        PIValueTextView.setText("-.-");
//                    } else {
//                        PIValueTextView.setText("" + PI);
//                    }
                    // display resp value
                    int respValue = Integer.parseInt(receivedData.substring(76, 78), 16);
                    Log.d("respValue",""+respValue);
                    onM70cDataListener.setRespValue(respValue);
                    short[] r = new short[4];
                    r[0] = (short) ((short) (Short.parseShort(receivedData.substring(80, 82), 16) << 8)
                            + Short.parseShort(receivedData.substring(78, 80), 16));

                    r[1] = (short) ((short) (Short.parseShort(receivedData.substring(84, 86), 16) << 8)
                            + Short.parseShort(receivedData.substring(82, 84), 16));

                    r[2] = (short) ((short) (Short.parseShort(receivedData.substring(88, 90), 16) << 8)
                            + Short.parseShort(receivedData.substring(86, 88), 16));

                    r[3] = (short) ((short) (Short.parseShort(receivedData.substring(92, 94), 16) << 8)
                            + Short.parseShort(receivedData.substring(90, 92), 16));
                    Log.d("the value of short","r0"+r[0]+"r1"+r[1]+"r2"+r[2]+"r3"+r[3]+"");
//                    long time = System.currentTimeMillis() / 1000;
//                    sleepDatas.add(new SleepBlock(r, (short) SPO2Value, (short) heartRateValue, (short) respValue, time));
//                    if (historyCounter == 0) {
//                        Date mDate = new Date(System.currentTimeMillis());
//                        SimpleDateFormat sdff = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
//                        String mFormatedDate = sdff.format(mDate).substring(5, 19);
//                        Log.d("TAG", mFormatedDate);
//                        mHistoryDatas.add(0, new HistoryData(mFormatedDate, SPO2Value, heartRateValue, respValue));
////                        Oximet oximet = new Oximet();
////                        oximet.setSPO2(SPO2Value);
////                        oximet.setPR(heartRateValue);
////                        oximet.setRESP(respValue);
////                        oximetsTamp.add(oximet);
////                                    Message msg = new Message();
////                                    msg.what = MainActivity.SENDOXIVALUE;
////                                    msg.obj = oximet;
////                                    config.getMainHandler2().sendMessage(msg);//往主线程发送message
//                    }
                                /* 保存至文件中 */
//                    if (isSdCardExist()) {
//                        try {
//                            Date date = new Date(System.currentTimeMillis());
//                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                            String dateString = "Oximeter_data_" + format.format(date) + ".txt";
//                            Log.w("txt", dateString);
//                            File file = new File(getSdCardPath(),
//                                    dateString);
//                            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
//                            bw.write(String.valueOf(SPO2Value));
//                            bw.write("\r\n");
//                            bw.write(String.valueOf(heartRateValue));
//                            bw.write("\r\n");
//                            bw.write(String.valueOf(respValue));
//                            bw.write("\r\n");
//                            bw.write(String.valueOf(r[0]));
//                            bw.write("\r\n");
//                            bw.write(String.valueOf(r[1]));
//                            bw.write("\r\n");
//                            bw.write(String.valueOf(r[2]));
//                            bw.write("\r\n");
//                            bw.write(String.valueOf(r[3]));
//                            bw.write("\r\n");
//                            bw.write(String.valueOf(time));
//                            bw.write("\r\n");
//                            bw.flush();
//                            System.out.println("写入成功");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
                    String wave = receivedData.substring(94, receivedData.length() - 2);
                    Log.d("the valus of waves??",wave);
                    for (int i = 0; i < wave.length(); i = i + 2) {
                        String value = wave.substring(i, i + 2);
                        int v = Integer.parseInt(value, 16) & 0x7F;
                        SPO2WaveValues.add(v);
                    }
                    Log.d("TAG", "Values number is : " + SPO2WaveValues.size());
                    onM70cDataListener.setSPo2ValuesPIValues(SPO2WaveValues,PIValues);
//                    Log.d("TAG", "Values number is : " + SPO2WaveValues.size());
                    receivedData = data;
                }
                receivedData = data;

                isFirstReceive = false;
            } else if (dataID.equals("43")) {
                // deal with battery
                Log.d("ll", "battery state is :" + data.substring(6, 8));
//

                     main = data.substring(8, 9) + "." + data.substring(9, 10);
                     sub = data.substring(10, 11) + "." + data.substring(11, 12);
                     battery=data.substring(6, 8);
//                     onM70cDataListener.setMain(main);
//                onM70cDataListener.setSub(sub);
//
//                onM70cDataListener.setbattery(rawData.substring(6, 8));
//                    currentDevice.setMainVersion(main);
//                    currentDevice.setSubVersion(sub);
//                }

            }
            onM70cDataListener.setMain(main);
            onM70cDataListener.setSub(sub);
            onM70cDataListener.setbattery(battery);

        } else {

            receivedData = receivedData + data;
            Log.d("to see the final receivedData", "" + receivedData);
        }
    }
}
