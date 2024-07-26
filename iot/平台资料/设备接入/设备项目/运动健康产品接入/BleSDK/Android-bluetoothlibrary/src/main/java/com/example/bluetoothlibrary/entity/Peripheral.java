package com.example.bluetoothlibrary.entity;

import java.io.Serializable;

public class Peripheral implements Serializable {//育儿宝
	private int id;
	private String name;//
	private String brand;//品牌
	private String model;//型号
	private String icon;
	private String manufacturer;//制造商
	private String bluetooth;//名字.m70c Bltwbp
	private int boundId;
	private int memberID;
	private String preipheralSN;//序列号
	private String remark;
	private float protocolVer;
	private float firmwareVer;//固件版本
	private String SubVersion;//从固件版本.血氧专用
	private String preipheralMAC;//硬件地址
	private String recordDate;
	private String createdDate;
	private String updatedDate;
	private int isActivation;
	private int webMode;//血压计里专用


	public String getSubVersion() {
		return SubVersion;
	}

	public void setSubVersion(String subVersion) {
		SubVersion = subVersion;
	}

	public int getWebMode() {
		return webMode;
	}

	public void setWebMode(int webMode) {
		this.webMode = webMode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getBluetooth() {
		return bluetooth;
	}

	public void setBluetooth(String bluetooth) {
		this.bluetooth = bluetooth;
	}

	public int getBoundId() {
		return boundId;
	}

	public void setBoundId(int boundId) {
		this.boundId = boundId;
	}

	public int getMemberID() {
		return memberID;
	}

	public void setMemberID(int memberID) {
		this.memberID = memberID;
	}

	public String getPreipheralSN() {
		return preipheralSN;
	}

	public void setPreipheralSN(String preipheralSN) {
		this.preipheralSN = preipheralSN;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public float getProtocolVer() {
		return protocolVer;
	}

	public void setProtocolVer(float protocolVer) {
		this.protocolVer = protocolVer;
	}

	public float getFirmwareVer() {
		return firmwareVer;
	}

	public void setFirmwareVer(float firmwareVer) {
		this.firmwareVer = firmwareVer;
	}

	public String getPreipheralMAC() {
		return preipheralMAC;
	}

	public void setPreipheralMAC(String preipheralMAC) {
		this.preipheralMAC = preipheralMAC;
	}

	public String getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(String recordDate) {
		this.recordDate = recordDate;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public int getIsActivation() {
		return isActivation;
	}

	public void setIsActivation(int isActivation) {
		this.isActivation = isActivation;
	}

    @Override
    public String toString() {
        return "llllllllllllllllllllllll: " + "name: " + name + ", preipheralSN: " + preipheralSN + ", model: " + model + ", preipheralMAC: " + preipheralMAC + ", protocolVer:" + protocolVer;
    }
}
