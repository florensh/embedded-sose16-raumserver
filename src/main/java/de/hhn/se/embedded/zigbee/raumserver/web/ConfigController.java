package de.hhn.se.embedded.zigbee.raumserver.web;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import de.hhn.se.embedded.zigbee.raumserver.Room;
import de.hhn.se.embedded.zigbee.raumserver.User;
import de.hhn.se.embedded.zigbee.raumserver.domain.Configuration;
import de.hhn.se.embedded.zigbee.raumserver.domain.ConfigurationRepository;

@Controller
public class ConfigController {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Value("${roomserver.id}")
	String roomserverId;

	@RequestMapping("/account")
	public String config(Model model) {
		model.addAttribute("name", "florens");

		ConfigDataObject data = new ConfigDataObject();

		Configuration username = configurationRepository
				.findByParamName("username");

		if (username != null) {
			data.setUsername(username.getParamValue());
		}

		model.addAttribute("configDataObject", data);
		return "account";
	}

	@RequestMapping("/")
	public String index(Model model) {

		Configuration username = configurationRepository
				.findByParamName("username");

		if (username != null) {
			model.addAttribute("username", username.getParamValue());
		}

		model.addAttribute("roomserverId", roomserverId);

		return "index";
	}

	@RequestMapping(value = "/account/save", method = RequestMethod.POST)
	public String submitGroupEditForm(
			@Valid final ConfigDataObject configDataObject,
			final BindingResult result, final ModelMap m,
			final Principal currentUser) {
		if (result.hasErrors()) {
			return "index";
		}

		Configuration username = configurationRepository
				.findByParamName("username");

		if (username != null) {
			username.setParamValue(configDataObject.getUsername());
			configurationRepository.save(username);
		} else {
			Configuration c = new Configuration();
			c.setParamName("username");
			c.setParamValue(configDataObject.getUsername());
			configurationRepository.save(c);
		}

		User user = new User();
		user.setUsername(configDataObject.getUsername());
		user.setPassword(configDataObject.getPassword());

		RestTemplate template = new RestTemplate();
		HttpEntity<User> request = new HttpEntity<User>(user);
		// String uri =
		// "https://enigmatic-waters-31128.herokuapp.com/api/login";
		String uri = "http://localhost:8081/api/login";
		HttpEntity<String> response = template.exchange(uri, HttpMethod.POST,
				request, String.class);

		String resultString = response.getBody();
		HttpHeaders headers = response.getHeaders();

		Configuration token = new Configuration();
		token.setParamName("authToken");
		token.setParamValue(headers.getFirst("X-AUTH-TOKEN"));
		this.configurationRepository.save(token);

		registerRoom();

		return "redirect:/";
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
		r.setRoomId(roomserverId);
		HttpEntity<Room> request = new HttpEntity<Room>(r, headers);
		// String uri =
		// "https://enigmatic-waters-31128.herokuapp.com/api/rooms/register";
		String uri = "http://localhost:8081/api/rooms/register";
		Room resp = restTemplate.postForObject(uri, request, Room.class);
		System.out.println(resp.getRoomId());

	}

}
