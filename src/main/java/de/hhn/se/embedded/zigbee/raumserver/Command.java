package de.hhn.se.embedded.zigbee.raumserver;

public class Command {
	
	private String target;
	
	private String type;
	
	private float value;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
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
