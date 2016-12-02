package de.hhn.se.embedded.zigbee.raumserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDevice;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDeviceImpl;

@Configuration
@Profile("prod")
public class ProdConfiguration {

	@Bean
	TemperatureSensor temperatureSensor() {
		try {
			return new TemperatureSensorImpl();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Bean
	TemperatureController temperatureController() {
		return new XBeeTemperatureController();

	}
	

	
	@Bean
	ZigBeeDevice zigBeeDevice() {
		return new ZigBeeDeviceImpl();
	}

}
