package de.hhn.se.embedded.zigbee.raumserver;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import de.hhn.se.embedded.zigbee.raumserver.domain.ConfigurationRepository;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device.Type;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.DeviceService;

public abstract class CommonTemperatureController implements
		TemperatureController, Observer {

	String cloudBackend = "https://enigmatic-waters-31128.herokuapp.com";
	String localhost = "http://localhost:8081";

	String backend = cloudBackend;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private TemperatureSensor tempSensor;

	@Autowired
	private DeviceRepository deviceRepository;

	private float targetTemperature = TemperatureController.DEFAULT_TEMPERATURE;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Value("${roomserver.id}")
	String roomserverId;

	private final Logger LOGGER = LoggerFactory
			.getLogger(TemperatureController.class);

	@Override
	public void setNewTargetTemperature(float temp) {
		try {
			if (temp != this.targetTemperature) {
				this.targetTemperature = temp;
				this.controlHeating(this.tempSensor.getTemp(),
						this.targetTemperature);

			}
		} catch (Exception e) {
			this.LOGGER.error("Error while setting new target temperature!", e);
		}

	}

	@PostConstruct
	private void doTempControlling() {
		this.tempSensor.addObserver(this);
	}

	@Override
	public void update(Observable arg0, Object arg1) {

		TemperatureSensor sensor = (TemperatureSensor) arg0;
		Float temp = sensor.getTemp();
		Float oldTemp = (Float) arg1;

		handleTemperatureChange(temp, oldTemp);

	}

	private void handleTemperatureChange(Float temp, Float oldTemp) {
		DecimalFormat df = new DecimalFormat("#.#");

		sendNewTemperatureToBackend(temp);
		controlHeating(temp, this.targetTemperature);

	}

	private void controlHeating(Float temp, Float target) {

		List<Device> heating_elements = this.deviceRepository
				.findByType("HEATING_ELEMENT");
		if (heating_elements != null && !heating_elements.isEmpty()) {
			LOGGER.info("calibrating heating with " + heating_elements.size()
					+ " heating elements [target: " + target + " / current: " + temp
					+ "]");

			byte val = (byte) (temp < target ? 100 : 0);

			if (temp > target) {
				val = 0;
			} else if (target - temp < 2) {
				val = 5;
			} else if (target - temp < 5) {
				val = 10;
			} else if (target - temp < 10) {
				val = 50;
			} else {
				val = 100;
			}

			for (Device thermo : heating_elements) {

				try {
					// this.zigBeeDevice.sendValue(thermo.getZigBeeAddress(),
					// val);
					doSendingToThermostat(thermo.getZigBeeAddress(), val);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	protected abstract void doSendingToThermostat(String zigBeeAddress, byte val);

	@Scheduled(fixedDelay = 10000)
	private void periodicTempUpdate() {
		this.sendNewTemperatureToBackend(this.tempSensor.getTemp());
	}

	@Async
	private void sendNewTemperatureToBackend(Float temp) {

		Device heating = this.deviceRepository.findOne(roomserverId + "_"
				+ Type.HEATING.name());
		if (heating != null) {
			this.LOGGER.info("sending temperature to backend");
			Device d = new Device();
			d.setDeviceId(heating.getDeviceId());
			d.setValue(temp);
			this.deviceService.updateDevice(d);

		}

	}

}
