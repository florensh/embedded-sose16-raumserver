package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;

public class DummyZigBeeDevice implements ZigBeeDevice {

	@Override
	public void sendValue(String address, byte b) throws TimeoutException,
			XBeeException {
		// ntbd!

	}

}
