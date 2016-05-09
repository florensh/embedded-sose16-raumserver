package de.hhn.se.embedded.zigbee.raumserver;

import java.util.Observer;

public interface TemperatureSensor {
	
	Float getTemp();
	
	void addObserver(Observer o);

}
