package de.hhn.se.embedded.zigbee.raumserver;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;
import de.hhn.se.embedded.zigbee.raumserver.web.UserService;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.DummyZigBeeDevice;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDevice;

@Configuration
@Profile("dev")
public class DevConfiguration {

	@Bean
	TemperatureSensor temperatureSensor() {
		return new DummyTemperatureSensor();

	}

	@Bean
	TemperatureController temperatureController() {
		return new DummyTemperatureController();

	}

	@Bean
	ZigBeeDevice zigBeeDevice() {
		return new DummyZigBeeDevice();
	}

	@Bean
	public InitializingBean addDummyDevice() {
		return new InitializingBean() {

			@Autowired
			UserService userService;

			@Autowired
			TemperatureSensor temperatureSensor;

			@Autowired
			DeviceRepository deviceRepository;

			@Value("${roomserver.id}")
			String roomId;

			@Override
			public void afterPropertiesSet() {

				addUser("user", "user");
				addDevice();

			}

			private void addDevice() {
				Device heating = new Device();
				heating.setDeviceId(this.roomId + "_HEATING");
				heating.setType("HEATING");
				heating.setTargetValue(TemperatureController.DEFAULT_TEMPERATURE);
				heating.setValue(this.temperatureSensor.getTemp());
				this.userService.registerDevice(heating);
				this.deviceRepository.save(heating);

			}

			private void addUser(String username, String password) {
				this.userService.registerUserAndRoom(username, password);

			}

		};

	}
}
