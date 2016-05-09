package de.hhn.se.embedded.zigbee.raumserver;

import javax.persistence.Id;


public class Device {
	public enum Type {
		HEATING, LIGHT
	}
	
	@Id
	private String deviceId;
	
	private String name;
	
	private String type;
	
	private Float value;
	
	private Float targetValue;
	
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