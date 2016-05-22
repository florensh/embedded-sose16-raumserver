package de.hhn.se.embedded.zigbee.raumserver.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Device")
public class Device {
	public enum Type {
		HEATING, SWITCH
	}
	
	@Id
	private String deviceId;
	
	private String name;
	
	private String type;
	
	private Float value;
	
	private Float targetValue;
	
	private String zigBeeAddress;
	
	private Float targetValueOnDevice;

	public Float getTargetValueOnDevice() {
		return targetValueOnDevice;
	}

	public void setTargetValueOnDevice(Float targetValueOnDevice) {
		this.targetValueOnDevice = targetValueOnDevice;
	}
	
    public String getZigBeeAddress() {
		return zigBeeAddress;
	}

	public void setZigBeeAddress(String zigBeeAddress) {
		this.zigBeeAddress = zigBeeAddress;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public Float getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(Float targetValue) {
		this.targetValue = targetValue;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}