package de.hhn.se.embedded.zigbee.raumserver;

public class Command {

	public enum CommandType {
		GET, SET
	}

	private String targetDevice;

	private String type;

	private float value;

	public String getTargetDevice() {
		return targetDevice;
	}

	public void setTargetDevice(String targetDevice) {
		this.targetDevice = targetDevice;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

}