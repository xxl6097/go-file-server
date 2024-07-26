package com.example.bluetoothlibrary.entity;

/**
 * Created by apple on 2016/10/19.
 */
public class ConnectBleServiceInfo {
    private String deviceName;
    private String serviceUUID;
    private String charateUUID;
    private String charateReadUUID;
    private String charateALiRealTimeUUID;
    private String charateALiBatteryUUID;
    private String charateALiHistoryDataUUID;
    private byte[] conectModel;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public String getCharateUUID() {
        return charateUUID;
    }

    public void setCharateUUID(String charateUUID) {
        this.charateUUID = charateUUID;
    }

    public String getCharateReadUUID() {
        return charateReadUUID;
    }

    public void setCharateReadUUID(String charateReadUUID) {
        this.charateReadUUID = charateReadUUID;
    }

    public byte[] getConectModel() {
        return conectModel;
    }

    public void setConectModel(byte[] conectModel) {
        this.conectModel = conectModel;
    }

    public String getCharateALiRealTimeUUID() {
        return charateALiRealTimeUUID;
    }

    public void setCharateALiRealTimeUUID(String charateALiRealTimeUUID) {
        this.charateALiRealTimeUUID = charateALiRealTimeUUID;
    }

    public String getCharateALiBatteryUUID() {
        return charateALiBatteryUUID;
    }

    public void setCharateALiBatteryUUID(String charateALiBatteryUUID) {
        this.charateALiBatteryUUID = charateALiBatteryUUID;
    }

    public String getCharateALiHistoryDataUUID() {
        return charateALiHistoryDataUUID;
    }

    public void setCharateALiHistoryDataUUID(String charateALiHistoryDataUUID) {
        this.charateALiHistoryDataUUID = charateALiHistoryDataUUID;
    }

    @Override
    public String toString() {
        return "kankan: deviceName: " + deviceName
                + ", serviceUUID: " + serviceUUID
                + ", charateUUID: " + charateUUID
                + ", charateReadUUID: " + charateReadUUID
                + ", charateALiRealTimeUUID" + charateALiRealTimeUUID;
    }
}
