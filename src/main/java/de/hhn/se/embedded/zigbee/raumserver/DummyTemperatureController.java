package de.hhn.se.embedded.zigbee.raumserver;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import javax.annotation.PostConstruct;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import de.hhn.se.embedded.zigbee.raumserver.Device.Type;
import de.hhn.se.embedded.zigbee.raumserver.domain.Configuration;
import de.hhn.se.embedded.zigbee.raumserver.domain.ConfigurationRepository;

public class DummyTemperatureController implements TemperatureController,
		Observer {

	@Autowired
	private TemperatureSensor tempSensor;

	private float targetTemperature = TemperatureController.DEFAULT_TEMPERATURE;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Value("${roomserver.id}")
	String roomserverId;

	@Override
	public void setNewTargetTemperature(float temp) {
		this.targetTemperature = temp;

	}

	@PostConstruct
	private void doTempControlling() {

		this.tempSensor.addObserver(this);

		(new Thread(new MyRunnable())).start();
	}

	class MyRunnable implements Runnable {

		public void run() {
			while (true) {

				System.out.println("current temperature is "
						+ tempSensor.getTemp() + ". Target is "
						+ targetTemperature);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {

		TemperatureSensor sensor = (TemperatureSensor) arg0;
		Float temp = sensor.getTemp();

		DecimalFormat df = new DecimalFormat("#.#");
		String sTemp = df.format(temp);

		Configuration token = configurationRepository
				.findByParamName("authToken");

		if (token != null) {
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
			headers.add("X-AUTH-TOKEN", token.getParamValue());
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJackson2HttpMessageConverter());

			String deviceUri = "http://localhost:8081/api/rooms/"
					+ roomserverId + "/devices/" + roomserverId + "_"
					+ Type.HEATING.name();
			Device d = new Device();
			d.setValue(temp);

			HttpClient httpClient = HttpClients.createDefault();
			restTemplate
					.setRequestFactory(new HttpComponentsClientHttpRequestFactory(
							httpClient));
			HttpEntity<Device> deviceRequest = new HttpEntity<Device>(d,
					headers);
			// restTemplate.put(deviceUri, deviceRequest);
			restTemplate.exchange(deviceUri, HttpMethod.PATCH, deviceRequest,
					Device.class);

		}

	}

}
