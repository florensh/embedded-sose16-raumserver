package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;

public interface ZigBeeDevice {

	void sendValue(String address, byte b) throws TimeoutException, XBeeException;

}
