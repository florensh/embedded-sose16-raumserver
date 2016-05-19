package de.hhn.se.embedded.zigbee.raumserver.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import de.hhn.se.embedded.zigbee.raumserver.Room;
import de.hhn.se.embedded.zigbee.raumserver.User;
import de.hhn.se.embedded.zigbee.raumserver.domain.Configuration;
import de.hhn.se.embedded.zigbee.raumserver.domain.ConfigurationRepository;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device.Type;

@Service
public class UserService {

	String cloudBackend = "https://enigmatic-waters-31128.herokuapp.com";
	String localhost = "http://localhost:8081";

	String backend = cloudBackend;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Value("${roomserver.id}")
	String roomserverId;

	public void registerUserAndRoom(String usern, String password) {
		Configuration username = configurationRepository
				.findByParamName("username");

		if (username != null) {
			username.setParamValue(usern);
			configurationRepository.save(username);
		} else {
			Configuration c = new Configuration();
			c.setParamName("username");
			c.setParamValue(usern);
			configurationRepository.save(c);
		}

		User user = new User();
		user.setUsername(usern);
		user.setPassword(password);

		RestTemplate template = new RestTemplate();
		HttpEntity<User> request = new HttpEntity<User>(user);
		// String uri =
		// "https://enigmatic-waters-31128.herokuapp.com/api/login";
		String uri = backend + "/api/login";
		HttpEntity<String> response = template.exchange(uri, HttpMethod.POST,
				request, String.class);

		String resultString = response.getBody();
		HttpHeaders headers = response.getHeaders();

		Configuration token = new Configuration();
		token.setParamName("authToken");
		token.setParamValue(headers.getFirst("X-AUTH-TOKEN"));
		this.configurationRepository.save(token);

		registerRoom();

	}

	public void registerDevice(Device d) {

		Configuration token = configurationRepository
				.findByParamName("authToken");

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("X-AUTH-TOKEN", token.getParamValue());
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());

		String deviceUri = backend + "/api/rooms/" + roomserverId + "/devices/"
				+ d.getDeviceId();

		HttpEntity<Device> deviceRequest = new HttpEntity<Device>(d, headers);
		restTemplate.put(deviceUri, deviceRequest);

	}

	private void registerRoom() {

		Configuration token = configurationRepository
				.findByParamName("authToken");

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("X-AUTH-TOKEN", token.getParamValue());
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());
		Room r = new Room();
		// r.setName("Testraum");
		HttpEntity<Room> roomRequest = new HttpEntity<Room>(r, headers);
		// String uri =
		// "https://enigmatic-waters-31128.herokuapp.com/api/rooms/register";
		String roomUri = backend + "/api/rooms/" + roomserverId;

		restTemplate.put(roomUri, roomRequest);

//		String deviceUri = backend + "/api/rooms/" + roomserverId + "/devices/"
//				+ roomserverId + "_" + Type.HEATING.name();
//		Device d = new Device();
//		d.setType(Type.HEATING.name());
//
//		HttpEntity<Device> deviceRequest = new HttpEntity<Device>(d, headers);
//		restTemplate.put(deviceUri, deviceRequest);


	}

}
