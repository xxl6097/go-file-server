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

package com.example.bluetoothlibrary.entity;




import java.util.ArrayList;
import java.util.List;

/**
 * This class includes a small subset of standard GATT attributes for
 * demonstration purposes.
 */
public class SampleGattAttributes {
	public static String SeviceIDfbb0 = "0000fff0-0000-1000-8000-00805f9b34fb";
	public static String GetCharacteristicIDfbb2 = "0000fff4-0000-1000-8000-00805f9b34fb";
	public static String SeviceIDfbb0_ALi = "00001810-0000-1000-8000-00805f9b34fb";
	public static String GetCharacteristicIDfbb2_ALi = "00002a35-0000-1000-8000-00805f9b34fb";
	public static String GetCharacteristicIDRealTime_ALi = "00002a36-0000-1000-8000-00805f9b34fb";
	public static String GetCharacteristicIDHistoryData_ALi = "0000fa35-0000-1000-8000-00805f9b34fb";
    public static String GetCharacteristicIDBattery_ALi = "00002a19-0000-1000-8000-00805f9b34fb";
	public static String GetCharacteristicIDfbb1 = "0000fff1-0000-1000-8000-00805f9b34fb";
	public static String GetCharacteristicID = "0000ffe1-0000-1000-8000-00805f9b34fb";
	// public static String SeviceIDfbb0 =
	// "0000fbb0-494c-4f47-4943-544543480000";
	// public static String GetCharacteristicIDfbb1 =
	// "0000fbb1-494c-4f47-4943-544543480000";
	// public static String GetCharacteristicIDfbb2 =
	// "0000fbb2-494c-4f47-4943-544543480000";
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	public static String CLIENT_CHARACTERISTIC_CONFIG_WF = "00002902-0000-1000-8000-00805f9b34fb";
//    public static String CLIENT_CHARACTERISTIC_CONFIG = "00001810-0000-1000-8000-00805f9b34fb";

	public static byte[] data = { (byte) 0xAA, 0x06, 0x78, 0x00, 0x00, 0x00, 0x00, 0x00 };// ���β���
	public static byte[] data2 = { (byte) 0xAA, 0x06, 0x78, 0x08, 0x00, 0x00, 0x00, 0x00 };// ��������

	public static byte[] data4 = { (byte) 0xAA, 0x05, (byte) 0x7A, 0x00, 0x00, 0x00, 0x00 };// 7.2.5.����ʼ������ʷ����(ID:122)
	public static byte[] data6 = { (byte) 0xAA, 0x04, (byte) 0x7F, 0x00, 0x00, 0x00 };// 7.2.7.�����������ݣ�ID:127��

	public static byte[] data7 = { (byte) 0xAA, 0x03, (byte) 0x79, 0x00, 0x00 };

//	public static byte[] data8 = { (byte) 0xAA, 0x05, (byte) 0x50, 0x04, 0x00,0x00,0x00 };


	
	public static List<BleData> listdata=new ArrayList<BleData>();
	/**
	 * ������ʷ�����ط�(ID:123)
	 */

	public static byte[] add(int b)
	{
		byte[] data8 = { (byte) 0xAA, 0x05, (byte) 0x50,  (byte) b, 0x00,0x00,0x00 };
		return data8;
	}
	public static byte[] resendBleData(int b1, int b2, int b3, int b4) {
		byte[] data = { (byte) 0xAA, 0x07, (byte) 0x7B, (byte)b1, (byte)b2, (byte)b3, (byte)b4, 0x00, 0x00 };// 7.2.6.������ʷ�����ط�(ID:123)
		return data;
	}




	/**
	 * ƥ������
	 * 
	 * @param deviceName
	 * @return
	 */
	public static boolean isEqualName(String deviceName) {
		return deviceName.equals(Constant.BLT_WBP) || deviceName.equals(Constant.AL_WBP);

	}
}
