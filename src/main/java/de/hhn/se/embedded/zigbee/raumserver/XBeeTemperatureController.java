package de.hhn.se.embedded.zigbee.raumserver;

import org.springframework.beans.factory.annotation.Autowired;

import com.digi.xbee.api.exceptions.XBeeException;

import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDevice;

public class XBeeTemperatureController extends CommonTemperatureController {

	@Autowired
	private ZigBeeDevice zigBeeDevice;

	@Override
	protected void doSendingToThermostat(String zigBeeAddress, byte val) {
		try {
			this.zigBeeDevice.sendValue(zigBeeAddress, val);
		} catch (XBeeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
