package de.hhn.se.embedded.zigbee.raumserver;

import java.util.Observer;

public interface TemperatureController {
	
	static final float DEFAULT_TEMPERATURE = 20;
	
	void setNewTargetTemperature(float temp);
	

}
