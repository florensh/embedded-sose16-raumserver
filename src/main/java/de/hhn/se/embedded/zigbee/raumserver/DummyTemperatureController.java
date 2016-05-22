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

import de.hhn.se.embedded.zigbee.raumserver.domain.ConfigurationRepository;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device.Type;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.DeviceService;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDevice;

public class DummyTemperatureController implements TemperatureController,
		Observer {

	String cloudBackend = "https://enigmatic-waters-31128.herokuapp.com";
	String localhost = "http://localhost:8081";

	String backend = cloudBackend;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private TemperatureSensor tempSensor;

	@Autowired
	private ZigBeeDevice zigBeeDevice;

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

		if (oldTemp != null) {
			// LOGGER.info("temperature changed from " + df.format(oldTemp)
			// + " to " + df.format(temp) + " degrees celcius");

		}

		sendNewTemperatureToBackend(temp);
		controlHeating(temp, this.targetTemperature);

	}

	private void controlHeating(Float temp, Float target) {

		List<Device> thermostats = this.deviceRepository
				.findByType("THERMOSTAT");
		if (thermostats != null && !thermostats.isEmpty()) {
			LOGGER.info("calibrating heating with " + thermostats.size()
					+ " thermostats [target: " + target + " / current: " + temp
					+ "]");

			byte val = (byte) (temp < target ? 100 : 0);

			for (Device thermo : thermostats) {

				try {
					this.zigBeeDevice.sendValue(thermo.getZigBeeAddress(), val);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	private void sendNewTemperatureToBackend(Float temp) {

		Device heating = this.deviceRepository.findOne(roomserverId + "_"
				+ Type.HEATING.name());
		if (heating != null) {

			heating.setValue(temp);
			this.deviceService.updateDevice(heating);

			// Configuration token = configurationRepository
			// .findByParamName("authToken");
			//
			// if (token != null) {
			// MultiValueMap<String, String> headers = new
			// LinkedMultiValueMap<String, String>();
			// headers.add("X-AUTH-TOKEN", token.getParamValue());
			// RestTemplate restTemplate = new RestTemplate();
			// restTemplate.getMessageConverters().add(
			// new MappingJackson2HttpMessageConverter());
			//
			// String deviceUri = backend + "/api/rooms/" + roomserverId
			// + "/devices/" + heating.getDeviceId();
			// Device d = new Device();
			// d.setValue(temp);
			//
			// HttpClient httpClient = HttpClients.createDefault();
			// restTemplate
			// .setRequestFactory(new HttpComponentsClientHttpRequestFactory(
			// httpClient));
			// HttpEntity<Device> deviceRequest = new HttpEntity<Device>(d,
			// headers);
			// // restTemplate.put(deviceUri, deviceRequest);
			// restTemplate.exchange(deviceUri, HttpMethod.PATCH,
			// deviceRequest, Device.class);
			//
			// }

		}

	}

}
