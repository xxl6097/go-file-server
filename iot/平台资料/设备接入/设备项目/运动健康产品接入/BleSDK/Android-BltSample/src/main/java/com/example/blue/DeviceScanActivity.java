/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.blue;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.bluetoothlibrary.BluetoothLeClass;
import com.example.bluetoothlibrary.Config;
import com.example.bluetoothlibrary.Impl.ResolveM70c;
import com.example.bluetoothlibrary.Impl.ResolveWbp;
import com.example.bluetoothlibrary.Impl.ResolveWf100;
import com.example.bluetoothlibrary.Impl.ResolveWt1;
import com.example.bluetoothlibrary.Impl.ResolveWt2;
import com.example.bluetoothlibrary.LocationUtils;

import com.example.bluetoothlibrary.SettingUtil;
import com.example.bluetoothlibrary.Utils;
import com.example.bluetoothlibrary.entity.ConnectBleServiceInfo;
import com.example.bluetoothlibrary.entity.Constant;
import com.example.bluetoothlibrary.entity.HistoryData;
import com.example.bluetoothlibrary.entity.Peripheral;
import com.example.bluetoothlibrary.entity.SampleGattAttributes;
import com.example.bluetoothlibrary.entity.SleepBlock;
import com.example.bluetoothlibrary.entity.SycnBp;
import com.example.bluetoothlibrary.entity.SycnData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.Vector;

import static com.example.bluetoothlibrary.BluetoothLeClass.GetCharacteristicID;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends Activity implements AdapterView.OnItemClickListener {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();
    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private final static String UUID_KEY_DATA_WF = "0000fff4-0000-1000-8000-00805f9b34fb";
    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    public static DatalistAdapter datalistAdapter;
    public  ArrayList<SycnData> sycnDatas;
    public static int WBPMODE = -1;//

    private BluetoothAdapter mBluetoothAdapter;

    private boolean isActivityFront = true;
    public static int TEMPSTATE = 0;
    public static final int MACRO_CODE_1 = 1;
    public static final int MACRO_CODE_2 = 2;
    public static final int MACRO_CODE_3 = 3;//显示温度
    public static final int MACRO_CODE_4 = 4;//
    public static final int MACRO_CODE_5 = 5;
    public static final int MACRO_CODE_6 = 6;
    public static final int MACRO_CODE_7 = 7;
    public static final int MACRO_CODE_8 = 8;
    public static final int MACRO_CODE_9 = 9;
    public static final int MACRO_CODE_10 = 10;
    public static final int MACRO_CODE_11 = 11;
    public static final int MACRO_CODE_12 = 12;
    public static final int MACRO_CODE_13 = 13;
    public static final int MACRO_CODE_14 = 14;
    public static final int MACRO_CODE_15 = 15;
    public static final int MACRO_CODE_16 = 16;
    public static final int MACRO_CODE_17= 17;
    public static final int MACRO_CODE_18= 18;
    public static final int MACRO_CODE_19= 19;
    public static final int MACRO_CODE_20= 20;
    public static final int MACRO_CODE_21= 21;
    public static final int MACRO_CODE_22= 22;
    public static final int MACRO_CODE_23= 23;
    private final int REQUEST_ENABLE_BT = 0xa01;
    public static boolean isHasPermissions = false;

    int currentapiVersion=android.os.Build.VERSION.SDK_INT;
    private BluetoothLeClass mBLE;
    private Config config=new Config();
    public static ArrayList<Peripheral> preipheralCopy = new ArrayList<Peripheral>();

    protected Handler handler;
    public ListView listView,datalist;
    private Button button,button_time,voiceSet,start_wbp,user_send;
    private TextView temp_Blt, spo2, heart_rate, pi, resvalue,version_show,testtime,error,bettray,tatol,temp_blan,wf,voice_tx,blt_state,wbpmode,work_mode,user,softwarevervion;
    private TextView sys,dia,hr,isguestmode,bp_mesure;
    private LinearLayout wbp_linearlayout;
    public double temp;
    public int spo2_s, heart_rate_s,resvalue_s,wbp,sys_int,dia_int,hr_int;
   public  boolean guestmode;
    public float pi_s;
    private LinearLayout oxi_linearlayout, temp_layout;
    private List<Byte> bytes = new ArrayList<Byte>();
    public  LeDeviceListAdapter mLeDeviceListAdapter;
    public static int BTBattery = 4;
    //this is the data to draw spo2wave
    private Vector<Integer> SPO2WaveValues;
    private Vector<Integer> PIValues;



    private ConnectBleServiceInfo connectServiceInfo;
    private SPO2WaveView mSPO2WaveView;
    public int i = 0;
    public int j=2;
     public boolean openble=true;
    WbpDatalistAdapter wbpDatalistAdapter;
    ResolveWt1 resolvewt1=new ResolveWt1();
    ResolveM70c resolveM70c=new ResolveM70c();
    ResolveWf100 resolveWf100=new ResolveWf100();
    ResolveWt2 resolveWt2=new ResolveWt2();
    ResolveWbp resolveWbp=new ResolveWbp();
//
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        if (Integer.valueOf(android.os.Build.VERSION.SDK)>=23)
        {
            getBLEPermissions();
        }
        initview();
        //set listenner
        initListener();

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

//         Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else  {
            if (!mBluetoothAdapter.isEnabled()) {
                openble=false;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }

        }
        //开启蓝牙
//        mBluetoothAdapter.enable();
        mBLE = new BluetoothLeClass(this);
        mBLE.setBluetoothGattCallback();
        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }

        if (mBluetoothAdapter.isEnabled())
        {
            mBLE.scanLeDevice(true);//start to scan

        }
        // set callback function
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        mBLE.setOnsetDevicePreipheral(mOnSetDevicePreipheral);
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
        mBLE.setOnConnectListener(mOnConnectlistener);
        mBLE.setOnDisconnectListener(mOndisconnectListener);
        resolvewt1.setOnWt1DataListener(onWt1DataListener);
        resolveWt2.setOnWt2DataListener(onWt2DataListener);
        resolveM70c.setOnM70cDataListener(onM70cDataListener);
        resolveWbp.setOnWBPDataListener(onWBPDataListener);
        resolveWf100.setOnWF100DataListener(onWf100DataListener);

        initHandler();

    }
    private void getBLEPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isHasPermissions = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
            }
        }else
        {
            isHasPermissions=true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == -1) {
            isHasPermissions = false;
        }else {
            isHasPermissions = true;
        }

    }



    public void initview() {
            SPO2WaveValues = new Vector<Integer>();
            PIValues=new Vector<Integer>();
            mSPO2WaveView = (SPO2WaveView) findViewById(R.id.SPO2Wave);
            mSPO2WaveView.setValues(SPO2WaveValues);
            mSPO2WaveView.setPIValues(PIValues);
            mSPO2WaveView.setZOrderOnTop(true);    // necessary
            SurfaceHolder sfhTrackHolder = mSPO2WaveView.getHolder();
            sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
            button=$(R.id.sycn);
            button_time=$(R.id.sycn_time);
            version_show=$(R.id.version);
            temp_blan=$(R.id.temp_blan);
            testtime= $(R.id.time);
            error= $(R.id.error);
            bettray=$(R.id.bettray);
            wf=$(R.id.wf);
            voice_tx=$(R.id.voice);
            voiceSet=$(R.id.set_voice);
            blt_state=$(R.id.blt_state);
            wbpmode=$(R.id.wbpmode);
            work_mode=$(R.id.work_mode);
            tatol=$(R.id.total);
            //血压计的数据展
            //显示血压结果
            wbp_linearlayout=$(R.id.wbp);
            sys= $(R.id.sys_id);
            dia= $(R.id.dia_id);
            hr= $(R.id.hr_id);
            isguestmode= $(R.id.isguestmode_id);
            bp_mesure=$(R.id.bp_mesure);
            start_wbp=$(R.id.wbp_start);
            user_send=$(R.id.user_send);
            listView =$(R.id.devive_list);//this for device
            datalist=$(R.id.datas);//this is for sync data from wbp
            temp_Blt = $(R.id.temp);
            oxi_linearlayout = $(R.id.oxi);
            spo2 = $(R.id.spo2);
            heart_rate = $(R.id.heart_rate);
            pi = $(R.id.pi);
            user=$(R.id.user);
            resvalue = $(R.id.resvalue);
            temp_layout = $(R.id.temp_layout);
            softwarevervion=$(R.id.softwarevervion);
            temp_Blt.setVisibility(View.INVISIBLE);
            listView.setOnItemClickListener(this);
            softwarevervion.setText(""+getAppVersionName(getApplicationContext()));

    }

  public void initListener() {

      //this is to get data .
      button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              //start to sync data
              String  connectingDevice=config.getConnectPreipheralOpsition().getBluetooth();
              if (connectingDevice.equals(Constant.BLT_WBP))
              {
                  resolveWbp.SendForAll(mBLE);
              }else {

                  resolvewt1.SendForAll(mBLE);
              }
          }
      });
      button_time.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              //start to sync time
              String  connectingDevice=config.getConnectPreipheralOpsition().getBluetooth();
              if (connectingDevice.equals(Constant.BLT_WBP))
              {
                  resolveWbp.getNowDateTime(mBLE);
              }else if (connectingDevice.equals(Constant.BLT_WT2)){

                  resolveWt2.SendForTime(mBLE);
              }else
              {
                  resolvewt1.SendForTime(mBLE);
              }
          }
      });

      //Wf100 to set voice.
      voiceSet.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {Log.e("lllll", "onclick");
              i++;

              if (i == 6) {
                  i = 0;
                  i=0x00;
              }
              if (i==1)
              {
                  i=0x01;
              }else if (i==2)
              {
                  i=0x02;
              }else if (i==3)
              {
                  i=0x03;
              }else if (i==4)
              {
                  i=0x04;
              }else if(i==5){

                  i=0x05;
              }else
              {
                  i = 0;
                  i=0x00;
              }
              Log.e("i","::"+i);
              resolveWf100.SetVoice(mBLE, SampleGattAttributes.add(i));
          }
      });
      if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
          Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
          finish();
      }
      //  wbp start measure and stop measure;
      start_wbp.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Log.e("....","start test");
              if (j%2==0){
                  resolveWbp.onSingleCommand(mBLE);
              }else
              {
                  resolveWbp.onStopBleCommand(mBLE);
              }
              j++;
          }
      });

      // wbp to change user
      user_send.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              j++;
              if (j%2==0)
              {
                  SettingUtil.userModeSelect=1;
              }
              else {
                  SettingUtil.userModeSelect=2;
              }


              resolveWbp.sendUserInfoToBle(mBLE);
          }
      });

  }



    private void initHandler() {
        handler = new Handler() {

            @Override
            public void dispatchMessage(Message msg) {
                switch (msg.what) {
                    case MACRO_CODE_1://show the device list
                        listView.setAdapter(mLeDeviceListAdapter);
                        if (mLeDeviceListAdapter != null) {
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                        break;
                    case MACRO_CODE_2:
                        listView.setAdapter(mLeDeviceListAdapter);
                        if (mLeDeviceListAdapter != null) {
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                        break;
                    case MACRO_CODE_3://wt1 temp
                        listView.setVisibility(View.GONE);
                        temp_Blt.setVisibility(View.VISIBLE);
                        temp =(Double) msg.obj;
                        temp_Blt.setText("" +formatDouble4(temp));
                        break;
                    case MACRO_CODE_4://temp state high
                        listView.setVisibility(View.GONE);
                        temp_Blt.setVisibility(View.VISIBLE);
                        temp_Blt.setText("temp high");
                        break;
                    case MACRO_CODE_5://temp state low
                        listView.setVisibility(View.GONE);
                        temp_Blt.setVisibility(View.VISIBLE);
                        temp_Blt.setText("temp low");
                        break;
                    case MACRO_CODE_6://wt2 unblance templistView.setVisibility(View.GONE);
                        temp_Blt.setVisibility(View.VISIBLE);
                        temp = (Double) msg.obj;
                        temp_Blt.setText("" + formatDouble4(temp));
                        break;
                    case MACRO_CODE_7://wt2 balance temp
                        listView.setVisibility(View.GONE);
                        temp_Blt.setVisibility(View.VISIBLE);
                        bettray.setText(""+msg.arg1);
                        temp = (Double) msg.obj;
                        temp_blan.setText("" + formatDouble4(temp));
                        break;
                    case MACRO_CODE_8://oxygen value
                        listView.setVisibility(View.GONE);
                        temp_layout.setVisibility(View.GONE);
                        spo2_s = (int) msg.obj;
                        spo2.setText("" + spo2_s);
                        break;
                    case MACRO_CODE_9://W70
                        listView.setVisibility(View.GONE);
                        temp_layout.setVisibility(View.GONE);
                        heart_rate_s = (int) msg.obj;
                        heart_rate.setText("" + heart_rate_s);
                        break;
                    case MACRO_CODE_10://w70pi
                        listView.setVisibility(View.GONE);
                        temp_layout.setVisibility(View.GONE);
                        pi_s = (Float) msg.obj;
                        pi.setText("" + pi_s);
                        break;
                    case MACRO_CODE_11://w70res
                        listView.setVisibility(View.GONE);
                        temp_layout.setVisibility(View.GONE);
                        resvalue_s = (int) msg.obj;
                        resvalue.setText("" + resvalue_s);
                        break;
                    case MACRO_CODE_12://Cuff pressure
                        listView.setVisibility(View.GONE);

                                bp_mesure.setText(""+msg.arg1);

                        break;
                    case MACRO_CODE_13://web the final result
                        listView.setVisibility(View.GONE);
                                sys.setText(""+ msg.arg1);
                                dia.setText(""+msg.arg2);
                                hr.setText(""+msg.obj);
                        String str2 = msg.getData().getString("isguestmode");
                                isguestmode.setText(""+str2);
                        break;
                    case MACRO_CODE_14://the state and version of the wbp
                        listView.setVisibility(View.GONE);
                        bettray.setText(""+msg.arg1);
                        String str_version = msg.getData().getString("version");
                        version_show.setText(str_version);
                        String str_bleState = msg.getData().getString("bleState");
                        Log.e("str_bleState",str_bleState);
                        wbpmode.setText(str_bleState);
                        String str_devState = msg.getData().getString("devState");
                        Log.e("str_devState",str_devState);
                        work_mode.setText(str_devState);
                        break;
                    case MACRO_CODE_15://error
                        listView.setVisibility(View.GONE);
                        error.setText(""+msg.obj);
                    case MACRO_CODE_16://sync total datas
                        listView.setVisibility(View.GONE);
                        datalist.setAdapter(wbpDatalistAdapter);
                        if (wbpDatalistAdapter!=null)
                        {
                            wbpDatalistAdapter.notifyDataSetChanged();
                        }
                        tatol.setText(""+msg.arg1);
                        break;
                    case MACRO_CODE_17://softversion
                        listView.setVisibility(View.GONE);
                        Log.e("softversion",""+msg.obj);
                        version_show.setText(""+msg.obj);
                        break;
                    case MACRO_CODE_18://BTtime
                        listView.setVisibility(View.GONE);
                        Log.e("time",""+msg.obj);
                        testtime.setText(""+msg.obj);
                        break;
                    case MACRO_CODE_19://battery
                        listView.setVisibility(View.GONE);
                        Log.e("battery",""+msg.obj);
                        bettray.setText(""+msg.obj);
                        break;
                    case MACRO_CODE_20://fetal wf100
                        listView.setVisibility(View.GONE);
                        Log.e("fetal",""+msg.arg1);
                        wf.setText(""+msg.arg1);
                        break;
                    case MACRO_CODE_21://voice wf100
                        listView.setVisibility(View.GONE);
                        Log.e("voice",""+msg.obj);
                        voice_tx.setText(""+msg.obj);
                        break;
                    case MACRO_CODE_22://the state of wbp 0:spare 1:working
                        listView.setVisibility(View.GONE);
                        if (msg.arg1==0)
                        {
                            blt_state.setText("1");
                        }else {
                            blt_state.setText("0");
                        }
                        break;
                    case MACRO_CODE_23://the user information/wbp
                        listView.setVisibility(View.GONE);
                        if (msg.arg1==1)
                        {
                            user.setText("user 1");
                        }else {
                            user.setText("user 2");
                        }
                        break;
                    default:

                        break;
                }
            }
        };
        config.setMyFragmentHandler(handler);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!openble)
        {
            mBLE.scanLeDevice(true);
        }
        Log.e("see?",""+isActivityFront);
        isActivityFront = true;
    }

    @Override
    protected void onPause() {

        super.onPause();
        isActivityFront = false;
        if (mSPO2WaveView.getDrawing()) {
            mSPO2WaveView.stopDraw();
        }
    }

    @Override
    protected void onStop() {
//        mBLE.Unregister();
        super.onStop();
        mBLE.disconnect();
        mBLE.close();
        if (mLeDeviceListAdapter!=null)
        {

            mLeDeviceListAdapter.clear();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == listView) {
            final Peripheral device = mLeDeviceListAdapter.getDevice(position);
            if (device == null)
            {
                return;
            }
            else
            {

                mBLE.setBLEService(device.getPreipheralMAC());
                config.setConnectPreipheralOpsition(device);//set to be current device
                Log.e(" the current device", "" + config.getConnectPreipheralOpsition().getPreipheralMAC() + "" + config.getConnectPreipheralOpsition().getBluetooth());
                Log.e("the version of the device", "" + device.getModel());
            }
//            sendMsg(MACRO_CODE_3,handler,null);
        }
    }
    /**
     * 蓝牙连接上的状态
     *
     */


    BluetoothLeClass.OnsetDevicePreipheral mOnSetDevicePreipheral=new BluetoothLeClass.OnsetDevicePreipheral() {
        @Override
        public void setDevicePreipheral(BluetoothDevice device, int model, String SN, float protocolVer) {
            Peripheral preipheral = new Peripheral();
            preipheral.setBluetooth(device.getName());
            preipheral.setPreipheralMAC(device.getAddress());
            switch (model) {
                case 1:
                    preipheral.setModel("WT1");
                    break;
                case 2:
                    preipheral.setModel("WT2");
                    break;
                case 3:
                    preipheral.setModel("WT3");
                    break;
                case 48:
                    preipheral.setModel("M70C");
                    break;
                case 51:
                    preipheral.setModel("WBP202");
                    WBPMODE = 1;
                    break;
                case 57:
                    preipheral.setModel("WBP204");
                    WBPMODE = 1;
                    break;
                case 70:
                    preipheral.setModel("WF100");
                    break;
                case 71:
                    preipheral.setModel("WF200");
                    break;
                default:
                    break;
            }
            //it is just for wbp
            if (WBPMODE!=-1)
            {
                preipheral.setWebMode(WBPMODE);
            }
            preipheral.setPreipheralSN(SN);
            preipheral.setName("Smart thermometer");
            preipheral.setBrand("Wearcare");
            preipheral.setManufacturer("blt");
            preipheral.setIsActivation(0);
            preipheral.setProtocolVer(protocolVer);
            preipheral.setRemark("");
            synchronized (preipheralCopy) {
                if (preipheralCopy.size() == 0) {
                    preipheralCopy.add(preipheral);
                    mLeDeviceListAdapter=new LeDeviceListAdapter(DeviceScanActivity.this,preipheralCopy);
                    sendMsg(DeviceScanActivity.MACRO_CODE_1,handler,null);
                } else {
                    boolean isTrue = false;//
                    for (int i = 0; i < preipheralCopy.size(); i++) {
                        Peripheral preipheral3 = preipheralCopy.get(i);
                        if (preipheral3.getPreipheralSN().equals(SN)) {
                            isTrue = true;//穿在
                        }
                    }
                    //
                    if (!isTrue) {
                        preipheralCopy.add(preipheral);
                        mLeDeviceListAdapter=new LeDeviceListAdapter(DeviceScanActivity.this,preipheralCopy);

                        sendMsg(DeviceScanActivity.MACRO_CODE_2,handler,null);
                    }
                }
                Log.e("the connecting devie", preipheral.toString());
            }
        }
    };



       BluetoothLeClass.OnConnectListener mOnConnectlistener=new BluetoothLeClass.OnConnectListener() {
        @Override
        public void onConnect(BluetoothGatt gatt) {


            if (config.getConnectPreipheralOpsition().getModel().equals(Constant.M70C))
            {
                if (!mSPO2WaveView.getDrawing() && isActivityFront) {
                    mSPO2WaveView.startDraw();
                }

                PIValues = new Vector<>();
                mSPO2WaveView.setPIValues(PIValues);
            }


            Message msg = new Message();
            msg.what = MACRO_CODE_22;
            msg.arg1=0;
            config.getMyFragmentHandler().sendMessage(msg);

//            isConnecting=true;
        }


    };

    /**
     * the bluetoth is disconnected
     *
     */
    BluetoothLeClass.OnDisconnectListener mOndisconnectListener=new BluetoothLeClass.OnDisconnectListener() {
        @Override
        public void onDisconnect(BluetoothGatt gatt) {
//            blt_state.setText("已断开");

            mSPO2WaveView.stopDraw();

            Message msg = new Message();
            msg.what = MACRO_CODE_22;
            msg.arg1=1;
            config.getMyFragmentHandler().sendMessage(msg);


        }
    };



    /**
     * 数据解析之后Wt1的接收到的数据
     */
    public ResolveWt1.OnWt1DataListener onWt1DataListener=new ResolveWt1.OnWt1DataListener() {
        @Override
        public void setTemp(Double temp) {
            Message msg = new Message();
            msg.what = MACRO_CODE_3;//
            msg.obj = temp;//
            handler.sendMessage(msg);

        }
        @Override
        public void ontempState(int stateCode) {
            if (stateCode==4)
            {
                Message msg = new Message();
                msg.what = MACRO_CODE_4;
                config.getMyFragmentHandler().sendMessage(msg);//显示温度过高
            }
            else
            {
                Message msg = new Message();
                msg.what = MACRO_CODE_5;
                config.getMyFragmentHandler().sendMessage(msg);//显示温过低
            }

        }

        @Override
        public void onBTBattery(String bTBattery) {

            Message msg = new Message();
            msg.what = MACRO_CODE_19;
            msg.obj=bTBattery;
            config.getMyFragmentHandler().sendMessage(msg);//

        }

        @Override
        public void onVersion(String version) {
            Message msg = new Message();
            msg.what = MACRO_CODE_17;
            msg.obj=version;
            config.getMyFragmentHandler().sendMessage(msg);//

        }

        @Override
        public void onTime(String time) {
            Message msg = new Message();
            msg.what = MACRO_CODE_18;
            msg.obj=time;
            config.getMyFragmentHandler().sendMessage(msg);//

        }

        @Override
        public void ontotal(int total) {
            tatol.setText(total);
        }

        @Override
        public void onsycnResult(float BlueTem, String TempID) {

            SycnData sycnData=new SycnData();
            sycnData.setTemp(""+BlueTem);
            sycnData.setTempID(TempID);
            sycnDatas.add(sycnData);
            datalistAdapter=new DatalistAdapter(DeviceScanActivity.this,sycnDatas);
            datalist.setAdapter(datalistAdapter);
            datalistAdapter.notifyDataSetChanged();

        }

        @Override
        public void onSycnState(int begin, int total, String backtime) {
            testtime.setText(backtime);
            tatol.setText("开始"+begin+"总共"+total+"时间"+backtime);

        }
    };



    /**
     * 数据解析之后Wt2的接收到的数据
     */
    public ResolveWt2.OnWt2DataListener onWt2DataListener=new ResolveWt2.OnWt2DataListener() {
        @Override
        public void setUnbalanceTemp(Double unbalanceTemp) {

            Message msg = new Message();
            msg.what = MACRO_CODE_6;
            msg.obj=unbalanceTemp;
            config.getMyFragmentHandler().sendMessage(msg);//没到平衡的温度

        }

        @Override
        public void setBanlaceTemp(Double banlaceTemp,int btBattery) {

            Message msg = new Message();
            msg.what = MACRO_CODE_7;
            Log.e("bettery",""+btBattery);
            msg.arg1=btBattery;
            msg.obj=banlaceTemp;
            config.getMyFragmentHandler().sendMessage(msg);//平衡温度

        }


        @Override
        public void setWt2ver(String wt2ver) {
            Message msg = new Message();
            msg.what = MACRO_CODE_17;

            msg.obj=wt2ver;
            config.getMyFragmentHandler().sendMessage(msg);

        }

        @Override
        public void onsycnResult(String time) {
            Message msg = new Message();
            msg.what = MACRO_CODE_18;
            msg.obj=time;
            config.getMyFragmentHandler().sendMessage(msg);


        }

    };



    public ResolveM70c.OnM70cDataListener onM70cDataListener=new ResolveM70c.OnM70cDataListener() {
        @Override
        public void setSPO2Value(int spo2Value) {

            Message msg = new Message();
            msg.what = MACRO_CODE_8;
            msg.obj=spo2Value;
            handler.sendMessage(msg);

        }

        @Override
        public void setHeartRateValue(int heartRateValue) {

            Message msg = new Message();
            msg.what = MACRO_CODE_9;
            msg.obj=heartRateValue;
            handler.sendMessage(msg);

        }

        @Override
        public void setPI(Float pi) {
            Message msg = new Message();
            msg.what = MACRO_CODE_10;
            msg.obj=pi;
            handler.sendMessage(msg);
        }

        @Override
        public void setRespValue(int respValue) {

            Message msg = new Message();
            msg.what = MACRO_CODE_11;
            msg.obj=respValue;
            handler.sendMessage(msg);

        }

        @Override
        public void setbattery(String battery) {

            Log.e("battery::",""+battery);
            Message msg = new Message();
            msg.what = MACRO_CODE_19;
            msg.obj=battery;
            config.getMyFragmentHandler().sendMessage(msg);

        }

        @Override
        public void setMain(String main) {

            Log.e("mainversion::",""+main);
            Message msg = new Message();
            msg.what = MACRO_CODE_17;
            msg.obj=main;
            config.getMyFragmentHandler().sendMessage(msg);

        }

        @Override
        public void setSub(String sub) {

            Log.e("SUbversion::",""+sub);
        }

        @Override
        public void setSPo2ValuesPIValues(Vector<Integer> sp02WaveValues, Vector<Integer> piValues) {

            mSPO2WaveView.setValues(sp02WaveValues);
            mSPO2WaveView.setPIValues(piValues);



        }


    };



    public ResolveWbp.OnWBPDataListener onWBPDataListener=new ResolveWbp.OnWBPDataListener() {
        @Override
        public void onMeasurementBp(int temp) {
            //这个是实时的袖带压
            Log.e("temp........................",""+temp);
            Message msg = new Message();
            msg.what = MACRO_CODE_12;
            msg.arg1=temp;
            handler.sendMessage(msg);
        }
        @Override
        public void onMeasurementfin(final int SYS, final int DIA, final int PR, final Boolean isguestmode) {
          //这个是测量出来的结果

            Log.e("SYS........................",""+SYS);
            Log.e("DIA........................",""+DIA);
            Log.e("PR........................",""+PR);
            Log.e("isguestmode........................",""+isguestmode);
            Message msg = new Message();
            msg.what = MACRO_CODE_13;
            msg.arg1=SYS;
            msg.arg2=DIA;
            msg.obj=PR;
            Bundle bundle=new Bundle();
            bundle.putString("isguestmode",""+isguestmode);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }

        /**
         * when measure failed
         * @param obj
         */
        @Override
        public void onErroer(Object obj) {
            //这个是错误提示
        Log.e("error",""+obj);

            Message message=new Message();
            message.what = MACRO_CODE_15;
            message.obj=obj;
            handler.sendMessage(message);
        }

        /**
         *
         * @param btbattey  betttey
         * @param bleState   o: spare 1:working
         * @param version
         * @param devState  workmode
         */
        @Override
        public void onState(int btbattey, String bleState, String version, String devState) {
            //这个是状态
//            bettray.setText(""+btbattey);
            Log.e("bleState.............",""+bleState);
            Log.e("btbattey.............",""+btbattey);
//            version_show.setText(""+version);
            Log.e("devState.............",""+devState);
            Log.e("version.............",""+version);
            Message msg = new Message();
            msg.what = MACRO_CODE_14;
            msg.arg1=btbattey;
            Bundle bundle=new Bundle();
            bundle.putString("bleState",""+bleState);
            bundle.putString("version",""+version);
            bundle.putString("devState",""+devState);

            msg.setData(bundle);
            handler.sendMessage(msg);


        }

        /**
         * the data that is not send to the app,when bluetooth is reconnect ,it will coming here.
         * @param sycnBps
         */
        @Override
        public void onSycnBp(ArrayList<SycnBp> sycnBps) {

            for(SycnBp sycnBp:sycnBps)
            {
                Log.e("the sync data ","sys"+sycnBp.getSys()+"dia"+sycnBp.getDia()+"pr"+sycnBp.getHr()+"time"+sycnBp.getTime());
            }
           wbpDatalistAdapter=new WbpDatalistAdapter(DeviceScanActivity.this,sycnBps);
            Message message=new Message();
            message.arg1=sycnBps.size();
            message.what=MACRO_CODE_16;
            handler.sendMessage(message);

        }

        @Override
        public void onTime(String wbp_time) {
            Message message=new Message();
            message.obj=wbp_time;
            message.what=MACRO_CODE_18;
            handler.sendMessage(message);

        }

        /**
         *
         * @param user
         */
        @Override
        public void onUser(int user) {
            Log.e("user::","......"+user);
            Message message=new Message();
            message.arg1=user;
            message.what=MACRO_CODE_23;
            handler.sendMessage(message);

        }
    };


   ResolveWf100.OnWF100DataListener onWf100DataListener=new ResolveWf100.OnWF100DataListener() {

       /**
        * the received from device
        * @param time
        */
       @Override
       public void ontime(String time) {
           Message message=new Message();
           message.obj=time;
           message.what=MACRO_CODE_18;
           handler.sendMessage(message);

       }

       /**
        *
        * @param mainversion
        * @param subversion
        */
       @Override
       public void onverion(String mainversion, String subversion) {
           Log.e("mainversion",""+mainversion);
           Message msg = new Message();
           msg.what = MACRO_CODE_17;
           msg.obj=mainversion;
           config.getMyFragmentHandler().sendMessage(msg);

       }

       /**
        * the fetal value of wf100
        * @param Fr1
        */
       @Override
       public void onfr1(int Fr1) {


           Message msg = new Message();
           msg.what = MACRO_CODE_20;
           msg.arg1=Fr1;
           config.getMyFragmentHandler().sendMessage(msg);

       }

       /**
        *
        * @param battery
        */
       @Override
       public void onquantity(String battery) {
           Log.e("Bettery",""+battery);
           Message msg = new Message();
           msg.what = MACRO_CODE_19;
           msg.obj=battery;
           config.getMyFragmentHandler().sendMessage(msg);
       }

       /**
        *
        * @param voice the voice wf100
        */
       @Override
       public void Onvoice(String voice) {
           Message msg = new Message();
           msg.what = MACRO_CODE_21;
           msg.obj=voice;
           config.getMyFragmentHandler().sendMessage(msg);
       }
   };
    /**
     * when observe service then to diaplay the service.
     * no matter what device you want to connect ,just matching the device you want to de connect.do not to change anything.
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {
        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            Log.e("kkkkkkkkkkkkkkkkk", "found");
            connectServiceInfo = new ConnectBleServiceInfo();
            String  connectingDevice=config.getConnectPreipheralOpsition().getBluetooth();
            if (connectingDevice.equals(Constant.BLT_WBP)||connectingDevice.equals(Constant.AL_WBP)) {
                connectServiceInfo.setDeviceName(connectingDevice);
                connectServiceInfo.setServiceUUID(SampleGattAttributes.SeviceIDfbb0);
                connectServiceInfo.setCharateUUID(SampleGattAttributes.GetCharacteristicIDfbb2);
                connectServiceInfo.setCharateReadUUID(SampleGattAttributes.GetCharacteristicIDfbb1);
                connectServiceInfo.setConectModel(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            }else if (connectingDevice.equals(Constant.AL_WBP)) {
//                connectServiceInfo.setDeviceName(connectingDevice);
//                connectServiceInfo.setServiceUUID(SampleGattAttributes.SeviceIDfbb0_ALi);
//                connectServiceInfo.setCharateUUID(SampleGattAttributes.GetCharacteristicIDfbb2_ALi);
//                connectServiceInfo.setCharateReadUUID(SampleGattAttributes.GetCharacteristicIDfbb1);
//                connectServiceInfo.setCharateALiRealTimeUUID(SampleGattAttributes.GetCharacteristicIDRealTime_ALi);
//                connectServiceInfo.setCharateALiBatteryUUID(SampleGattAttributes.GetCharacteristicIDBattery_ALi);
//                connectServiceInfo.setCharateALiHistoryDataUUID(SampleGattAttributes.GetCharacteristicIDHistoryData_ALi);
//                connectServiceInfo.setConectModel(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            }
            if (connectingDevice.equals(Constant.BLT_WBP)||connectingDevice.equals(Constant.AL_WBP)) {
                //连接宝莱特的只需单通道通信
                displayGattServices(mBLE.getSupportedGattServices(), connectServiceInfo);
//            }else if (connectingDevice.equals(Constant.AL_WBP)) {
//                //连接阿里的需要多通道
//                displayGattServices_ali(mBLE.getSupportedGattServices());
            }else if ((connectingDevice.equals(Constant.BLT_WF1))){
                displayGattServices_WF(mBLE.getSupportedGattServices());
            }else
            {
                displayGattServices(mBLE.getSupportedGattServices());
            }
        }
    };
    /**
     *
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener() {
        /**
         * all data comes from here
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic) {

            Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                    + " read "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + Utils.bytesToHexString(characteristic.getValue()));
            if (GetCharacteristicID.equals(characteristic.getUuid())) {
                final byte[] datas = characteristic.getValue();
//                    Log.e("datas", "看看这个值是什么" + "" + datas);
                if (config.getConnectPreipheralOpsition().getModel().equals(Constant.WT2)) {
                    resolveWt2.calculateData_WT2(datas,mBLE,DeviceScanActivity.this);//to  resolve the data from wt2
                } else if (config.getConnectPreipheralOpsition().getModel().equals(Constant.M70C)) {
                    final byte[] data = characteristic.getValue();
                    if (data != null && data.length > 0) {
                        final StringBuilder stringBuilder = new StringBuilder(
                                data.length);
                        for (byte byteChar : data)
                            stringBuilder.append(String.format("%02X ", byteChar));
                        String s = stringBuilder.toString();
                        resolveM70c.calculateData_M70c(s);//resolve data from m70c
                    }
                } else
                {
                    resolvewt1.calculateData_WT1(datas,mBLE,DeviceScanActivity.this);//resolve data from wt1
                }
            } else {
                final byte[] data= characteristic.getValue();
                if (config.getConnectPreipheralOpsition().getBluetooth().equals(Constant.BLT_WBP)){

                   resolveWbp.resolveBPData2(data,mBLE,DeviceScanActivity.this);//resolve data from wep
                }else if (config.getConnectPreipheralOpsition().getBluetooth().equals(Constant.AL_WBP))
                {
                    resolveWbp.resolveALiBPData(data,getApplicationContext());//this is to resolve data from alibaba'device
                }else{

                    if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(
                            data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    String s =  stringBuilder.toString();
                        s=s.replace(" ","");
                    Log.e("s", "this is what" + "" + s);
                       resolveWf100.resolveBPData_wf(s);// this is to resolve data from wf100
                    }
                }
            }
        }
        /**
         * get the callback from write
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {

            Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + characteristic.getValue().toString());
        }
    };

    /**
     * to display the services of wf100,and to set notification
     * @param gattServices
     */
    private void displayGattServices_WF(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            //-----Service的字段信息-----//
            int type = gattService.getType();
            Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
            Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());

            //-----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));
                int property = gattCharacteristic.getProperties();
                Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));
//                byte[] data = gattCharacteristic.getValue();
//                if (data != null && data.length > 0) {
//                    Log.e(TAG, "---->char value:" + new String(data));
//                }
                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                Log.e("0000fff4-0000-1000-8000-00805f9b34fb", "=" + gattCharacteristic.getUuid().toString());
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA_WF)) {
                    //这下面就可以去开通知和读写数据
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mBLE.readCharacteristic(gattCharacteristic);
//                           }
//                        }, 500);
                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                    //设置数据内容
                    gattCharacteristic.setValue("send data->");
                    //往蓝牙模块写入数据
                    mBLE.writeCharacteristic(gattCharacteristic);
                }
                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));
                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:" + new String(desData));
                    }
                }
            }
        }//

    }

    //    this is to display the services of wt1
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            //-----Service-----//
            int type = gattService.getType();
            Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
            Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());

            //-----Characteristics-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));
                int property = gattCharacteristic.getProperties();
                Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));
                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG, "---->char value:" + new String(data));
                }
                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                Log.e("0000ffe4-0000-1000-8000-00805f9b34fb", "=" + gattCharacteristic.getUuid().toString());
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
                    //这下面就可以去开通知和读写数据
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mBLE.readCharacteristic(gattCharacteristic);
//                           }
//                        }, 500);
                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                    //设置数据内容
                    gattCharacteristic.setValue("send data->");
                    //往蓝牙模块写入数据
                    mBLE.writeCharacteristic(gattCharacteristic);
                }
                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));
                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:" + new String(desData));
                    }
                }
            }
        }//

    }

//  you can copy this to your project..if it is works.you will get some datas
    private void displayGattServices(List<BluetoothGattService> gattServices, ConnectBleServiceInfo serviceInfo) {
        if (gattServices == null)
            return;
        String uuid = null;
        Log.e("displayGattServices", serviceInfo.toString());
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            Log.e("uuid",""+gattService.getUuid().toString());
            if (serviceInfo.getServiceUUID().equals(uuid)) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    Log.e(" gattCharacteristic.getUuid().toString()",""+ gattCharacteristic.getUuid().toString());
                    if (uuid.equals(serviceInfo.getCharateReadUUID())) {
                        Log.e("连接GattCharacteUuid", uuid + ", CharacteSize: " + gattCharacteristics.size());
                        mBLE.setCharacteristicNotification(gattCharacteristic, true, serviceInfo);
                        mBLE.readCharacteristic(gattCharacteristic);
                        Log.e("--------1------", "11");
                        return;
                    }
                    if (uuid.equals(serviceInfo.getCharateUUID())) {

                        Log.e("连接GattCharacteUuid", uuid + ", CharacteSize: " + gattCharacteristics.size());
                        mBLE.setCharacteristicNotification(gattCharacteristic, true, serviceInfo);
                        return;
                    }
                }
            }

        }

    }

//this is for alibaba'device
    private void displayGattServices_ali(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if (uuid.contains("1810") || uuid.contains("180f")) {//1810:读最后测量数据, 实时数据 180f:读电量
                Log.e(TAG, "displayGattServices: " + uuid);
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    Log.e("console", "2gatt Characteristic: " + uuid);
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                    mBLE.readCharacteristic(gattCharacteristic);
                }
            }
        }
    }


    public <T> T $(int id) {
        return (T) findViewById(id);
    }
    //send data
    public static void sendMsg(int flag, Handler handler, Object object) {
        Message msg = new Message();
        msg.what = flag;
        msg.obj = object;
        if (handler!=null) {
            handler.sendMessage(msg);
        }
    }

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;

            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static String formatDouble4(double d) {
        DecimalFormat df = new DecimalFormat("#.00");


        return df.format(d);
    }


}





