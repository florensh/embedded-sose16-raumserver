package de.hhn.se.embedded.zigbee.raumserver;

public class DummyTemperatureSensor implements TemperatureSensor {

	@Override
	public float getValue() {
		return 18;
	}

}
