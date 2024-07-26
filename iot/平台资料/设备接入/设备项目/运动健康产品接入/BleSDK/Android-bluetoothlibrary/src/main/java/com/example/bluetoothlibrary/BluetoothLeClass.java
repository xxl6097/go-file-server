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

package com.example.bluetoothlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.bluetoothlibrary.entity.ConnectBleServiceInfo;
import com.example.bluetoothlibrary.entity.Constant;
import com.example.bluetoothlibrary.entity.Peripheral;
import com.example.bluetoothlibrary.entity.SampleGattAttributes;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeClass{
    private final static String TAG = BluetoothLeClass.class.getSimpleName();
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    public BluetoothGattCallback mGattCallback;
    private boolean mScanning = true;
    protected boolean isOnServiceConnected = false;
    public static String connectingDevice;//the connecting device
    protected BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothGattCharacteristic mCharacteristic;
    // gatt action...
    public final static String EXTRA_DATA = "com.blt.oximeter.EXTRA_DATA";
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetoothlibrary.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetoothlibrary.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetoothlibrary.ACTION_GATT_SERVICES_DISCOVERED ";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetoothlibrary.ACTION_DATA_AVAILABLE";
    public final static String ACTION_FAIL_TO_CONNECTED = "com.example.bluetoothlibrary.ACTION_FAIL_TO_CONNECTED";
    public final static String EXTRA_DATA_B = "com.example.bluetoothlibrary.EXTRA_DATA_BYTE";
    public final static String EXTRA_DATA_W = "com.example.bluetoothlibrary.EXTRA_DATA_WEIGHT";


    // UUID
    public final static String kReadUUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static String kServiceUUID = "0000ffe0-0000-1000-8000-00805f9b34fb";

    public final static String kReadUUID_wf = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static String kServiceUUID_wf = "0000ffe0-0000-1000-8000-00805f9b34fb";

    private final static String UUID_BLUETOOTH_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private final static String UUID_BLUETOOTH_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_BLUETOOTHLE_CHARACTERISTIC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID GetCharacteristicID = UUID.fromString(SampleGattAttributes.GetCharacteristicID);
	public interface OnConnectListener {
		public void onConnect(BluetoothGatt gatt);
	}
	public interface OnDisconnectListener {
		public void onDisconnect(BluetoothGatt gatt);
	}

	public interface OnServiceDiscoverListener {
		public void onServiceDiscover(BluetoothGatt gatt);
	}
	public interface OnDataAvailableListener {
		 public void onCharacteristicRead(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic);
		 public void onCharacteristicWrite(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic);

	}
    public interface  OnsetDevicePreipheral{

      public  void setDevicePreipheral(BluetoothDevice device, int model, String SN, float protocolVer);
    }

    private  OnsetDevicePreipheral onsetDevicePreipheral;
	private OnConnectListener mOnConnectListener;
	private OnDisconnectListener mOnDisconnectListener;
	private OnServiceDiscoverListener mOnServiceDiscoverListener;
	private OnDataAvailableListener mOnDataAvailableListener;
	private Context mContext;
	public void setOnConnectListener(OnConnectListener l){
		mOnConnectListener = l;
	}
	public void setOnDisconnectListener(OnDisconnectListener l){
		mOnDisconnectListener = l;
	}
	public void setOnServiceDiscoverListener(OnServiceDiscoverListener l){mOnServiceDiscoverListener = l;}
	public void setOnDataAvailableListener(OnDataAvailableListener l){
		mOnDataAvailableListener = l;
	}
    public void setOnsetDevicePreipheral(OnsetDevicePreipheral l){
        onsetDevicePreipheral = l;
    }

    public BluetoothLeClass(Context c){
		mContext = c;
	}

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    public void setLeScanCallback(){

        mLeScanCallback= new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                Log.e("device",device.toString());

                        if (device.getName() != null) {
                            switch (device.getName().trim()) {
                                case "BLT_M70C"://oximiter
                                    int type = scanRecord[12];
                                    Log.d(TAG, "type is " + type);
                                    String hexString = StringUtil.bytesToHexString(scanRecord);
                                    String sn = "";
                                    if (hexString != null && hexString.length() > (18 + 32)) {
                                        String hexStr = hexString.substring(18, 18 + 32);
                                        sn = DataCheckUtil.resolveBleMsg(hexStr);
                                    }
                                    onsetDevicePreipheral.setDevicePreipheral(device,type,sn,1);
                                    break;
                                case "BLT_MODT"://temp sitter
                                    StringBuilder SN = new StringBuilder();
                                    for (int i = 13; i < scanRecord.length; i++) {
                                        if (scanRecord[i] == 0)
                                            break;
                                        SN.append((char) scanRecord[i]);
                                    }
                                    onsetDevicePreipheral.setDevicePreipheral(device, scanRecord[12], SN.toString(), scanRecord[10]);
                                    break;
                                case "AL_WT1"://alibaba temp sitter
                                    String hexStrings = StringUtil.bytesToHexString(scanRecord);
                                    if (hexStrings != null && hexStrings.length() > (64 + 32)) {
                                        String hexStr = hexStrings.substring(64, 64 + 32);
                                        resolveBleMsg(device, hexStr);
                                    }
                                    break;
                                case "BLT_WT2"://
                                    StringBuilder SN_b = new StringBuilder();
                                    for (int i = 13; i < scanRecord.length; i++) {
                                        if (scanRecord[i] == 0)
                                            break;
                                        SN_b.append((char) scanRecord[i]);
                                    }
                                    onsetDevicePreipheral.setDevicePreipheral(device, scanRecord[12], SN_b.toString(), scanRecord[10]);
                                    break;
                                case "AL_WT2":
                                    String hexStrings_1 = StringUtil.bytesToHexString(scanRecord);
                                    if (hexStrings_1 != null && hexStrings_1.length() > (64 + 32)) {
                                        String hexStr = hexStrings_1.substring(64, 64 + 32);
                                        resolveBleMsg(device, hexStr);
                                    }
                                    break;
                                case "BLT_WBP"://blt bloodpresure
                                    StringBuilder SN_bp = new StringBuilder();
                                    for (int i = 13; i < scanRecord.length; i++) {
                                        if (scanRecord[i] == 0)
                                            break;
                                        SN_bp.append((char) scanRecord[i]);
                                    }
                                   onsetDevicePreipheral.setDevicePreipheral(device, scanRecord[12], SN_bp.toString(), scanRecord[10]);
                                    break;
                                case "AL_WBP":
                                    String hexString_al = StringUtill.bytesToHexString(scanRecord);
                                    if (hexString_al != null && hexString_al.length() > (64 + 32)) {
                                        String hexStr_al = hexString_al.substring(64, 64 + 32);
                                        Peripheral peripheral = DataCheckUtil.resolveBleMsg_bp(hexStr_al);
                                        onsetDevicePreipheral.setDevicePreipheral(device, Integer.parseInt(peripheral.getModel()), peripheral.getPreipheralSN(), peripheral.getProtocolVer());
                                    }
                                    break;
                                case "BLT_WF1":// Fetal monitoring
                                    StringBuilder SN_WF = new StringBuilder();
                                    for (int i = 13; i < scanRecord.length; i++) {
                                        if (i == 25)
                                            break;
                                        SN_WF.append((char) scanRecord[i]);

                                    }
                                    onsetDevicePreipheral.setDevicePreipheral(device, scanRecord[12], SN_WF.toString(), scanRecord[10]);
                                    break;

                                default:
                                    break;
                            }
                        }
            }
        };

    }
    //  if the device is provide by alibaba then use this mothod
    private void resolveBleMsg(BluetoothDevice device, String hexStr) {
        //00010001313530363031363533000000
        int lenth = 2;
        int i = 4;
        String currentResult = "";
        while (i * lenth < hexStr.length()) {
            String targetStr = "";
            if (i * lenth + lenth > hexStr.length()) {
                targetStr = hexStr.substring(i * lenth, hexStr.length() );
            }else {
                targetStr = hexStr.substring(i * lenth, i * lenth + lenth );
            }

            long hexValue = StringUtill.strtoul(targetStr, 16);
            int value = Integer.valueOf(String.valueOf(hexValue));
            if ((char)value == 0) {
                break;
            }
            currentResult = currentResult.concat((char) value + "");
            i++;
        }
        long protocolVersion = StringUtill.strtoul(hexStr.substring(2, 4), 16);
        long deviceModel = StringUtill.strtoul(hexStr.substring(6, 8), 16);
        onsetDevicePreipheral.setDevicePreipheral(device, (int) deviceModel, currentResult, protocolVersion);
    }




    /*
     * blurtooth scan
     */
    public void scanLeDevice(final boolean enable) {
        if (mBluetoothAdapter != null) {
            if (enable) {
                Log.d("test", " =scanLeDevice。open scan。。。。。。。。::");
                mBluetoothAdapter.startLeScan(mLeScanCallback);// BTState = 3;
                mScanning = true;
            } else {
                Log.d("test", " =scanLeDevice。 stop scan。。。。。。。。::");
                if (mLeScanCallback != null)
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
        }
    }
    public void setBluetoothGattCallback() {
        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String intentAction;
                Log.d(TAG, "onConnectionStateChange:-change--");
                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    intentAction = ACTION_GATT_CONNECTED;
                    // BTState = 2;
//                    broadcastUpdate(intentAction);
                    mOnConnectListener.onConnect(gatt);

                    Log.d(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                    // if (isOnSeerviceConnected) {
                    // MyHandlerUtil.sendMsg(Macro.MACRO_CODE_2,
                    // config.getBPMeasurementFragment(), null);
                    //
                    // }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                    Log.d(TAG, "Disconnected from GATT server.");
                    mOnDisconnectListener.onDisconnect(gatt);
//  intentAction = ACTION_GATT_DISCONNECTED;
//                    broadcastUpdate(intentAction);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                System.out.println("onServicesDiscovered..............=============");
                Log.d(TAG, "onServicesDiscovered:---");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    mOnServiceDiscoverListener.onServiceDiscover(gatt);
                } else {
                    Log.d(TAG, "onServicesDiscovered exception hanppen: " + status);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                System.out.println("onCharacteristicChanged..............===============");
//                Log.d(TAG, "onCharacteristicChanged:");
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                mOnDataAvailableListener.onCharacteristicRead(gatt,characteristic);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                System.out.println("onCharacteristicRead..............==========");
                Log.d("test", "onCharacteristicRead---");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("test", "BluetoothGatt.GATT_SUCCESS");
                    mOnDataAvailableListener.onCharacteristicRead(gatt,characteristic);

//                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }


            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                              int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                System.out.println("onCharacteristicWrite......receive........==========");
                Log.d("test", "onCharacteristicWrite----");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("test", "BluetoothGatt.GATT_SUCCESS");
                    mOnDataAvailableListener.onCharacteristicWrite(gatt,characteristic);
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.d("test", "onDescriptorWrite--");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Callback: Wrote GATT Descriptor successfully. " + "descriptor:" + descriptor.getCharacteristic().getUuid());
                } else {
                    Log.d(TAG, "Callback: Error writing GATT Descriptor: " + status + ", descriptor:" + descriptor.getCharacteristic().getUuid());
                }
            }
        };
    }
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();



        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        setLeScanCallback();
        return true;

    }



    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter is null");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                .fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            if (characteristic.getUuid().toString().contains("2a35")) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            }else {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.  我们自己公司的血压计 的开通知函
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled, ConnectBleServiceInfo serviceInfo) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            Log.d("tets", "tets/....setCharacteristicNotification.........null.......");
            return;
        }
        Log.d("tets", "characteristic.getUuid().toString()：：end" + characteristic.getUuid().toString());
        boolean flv = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        Log.d("tets", "notification success??::::::" + flv);
        // List<BluetoothGattDescriptor>
        // descriptors=characteristic.getDescriptors();
        // for(BluetoothGattDescriptor dp:descriptors){
        // dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        // mBluetoothGatt.writeDescriptor(dp);
        // }

        if (serviceInfo.getCharateReadUUID().equals(characteristic.getUuid().toString())
                || serviceInfo.getCharateUUID().equals(characteristic.getUuid().toString())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                    .fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptor.setValue(serviceInfo.getConectModel());
            if (mBluetoothGatt.writeDescriptor(descriptor)) {
                Log.d("tets", " mBluetoothGatt.writeDescriptor==true");
            } else {
                Log.d("tets", " mBluetoothGatt.writeDescriptor==false");
            }
        }
    }
//    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.d(TAG, "BluetoothAdapter为空");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
//                .fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//        if (descriptor != null) {
//            if (characteristic.getUuid().toString().contains("2a35")) {
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//            }else {
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            }
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
//    }

    /**
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;

            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");

        connectingDevice = device.getName();//正在链接的设备名
        mBluetoothDeviceAddress = address;//正在链接的设备地址
        return true;
    }
    /**
     * 连接地址
     *
     * @param mDeviceAddress
     */
    public synchronized void setBLEService(String mDeviceAddress) {
        System.out.println("mScanning.1..:" + mScanning);
        if (mScanning) {
            scanLeDevice(false);
        }
        System.out.println("isOnServiceConnected 1..." + isOnServiceConnected);
        if (!isOnServiceConnected) {
            isOnServiceConnected = connect(mDeviceAddress);
            System.out.println("isOnServiceConnected 2..." + isOnServiceConnected);
        }
    }
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
    	 mBluetoothGatt.writeCharacteristic(characteristic);
    }



    public void writeCharacteristic(byte[] data) {
        Log.d("test", "writeCharacteristic");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService myBluetoothGattService = null;

            myBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(kServiceUUID));
            if (myBluetoothGattService != null) {
                BluetoothGattCharacteristic mBluetoothGattCharacteristic = null;
                mBluetoothGattCharacteristic = myBluetoothGattService.getCharacteristic(UUID
                        .fromString(kReadUUID));
                mBluetoothGattCharacteristic.setValue(data);
                mBluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                Log.d("test", "writeCharacteristic::succed::");
            } else {

                Log.d("test", "writeCharacteristic::myBluetoothGattService == null");
            }



    }

    private byte[] crcCheck(byte[] datas) {
        // crc校验
        if (datas == null || datas.length < 3) {
            return null;
        }
        byte crc = 0x00;
        Log.d("test", "send-----------start-");
        for (int i = 1; i < datas.length - 1; i++) {
            Log.d("test", "send：：" + datas[i]);
            crc += datas[i];
        }
        Log.d("test", "send-----------end-");
        Log.d("test", "send crc：：" + crc);
        datas[datas.length - 1] = crc;
        return datas;
    }



    public void writeCharacteristic_wbp(byte[] data) {
        Log.w("test", "writeCharacteristic");
        if (mBluetoothAdapter == null || mBluetoothGatt == null || data == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (Constant.BLT_WBP.equals(connectingDevice)||Constant.BLT_WF1.equals(connectingDevice)) {
            BluetoothGattService myBluetoothGattService = null;
            myBluetoothGattService = mBluetoothGatt.getService(UUID.fromString(SampleGattAttributes.SeviceIDfbb0));
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = null;
            mBluetoothGattCharacteristic = myBluetoothGattService.getCharacteristic(UUID.fromString(SampleGattAttributes.GetCharacteristicIDfbb2));
            mBluetoothGattCharacteristic.setValue(crcCheck(data));
            mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
            Log.w("test", "writeCharacteristic：：end");
        }
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

//用来发送数据



}
