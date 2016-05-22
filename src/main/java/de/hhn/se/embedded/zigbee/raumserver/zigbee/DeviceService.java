package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import de.hhn.se.embedded.zigbee.raumserver.domain.Configuration;
import de.hhn.se.embedded.zigbee.raumserver.domain.ConfigurationRepository;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;

@Service
public class DeviceService {

	String cloudBackend = "https://enigmatic-waters-31128.herokuapp.com";
	String localhost = "http://localhost:8081";

	String backend = cloudBackend;

	@Value("${roomserver.id}")
	String roomId;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private ConfigurationRepository configurationRepository;

	public void updateDevice(Device device) {

		Configuration token = configurationRepository
				.findByParamName("authToken");

		if (token != null) {
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
			headers.add("X-AUTH-TOKEN", token.getParamValue());
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJackson2HttpMessageConverter());

			String deviceUri = backend + "/api/rooms/" + roomId + "/devices/"
					+ device.getDeviceId();
			Device d = new Device();
			d.setValue(device.getValue());
			d.setTargetValueOnDevice(device.getTargetValueOnDevice());

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
