package de.hhn.se.embedded.zigbee.raumserver.web;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

		Configuration password = configurationRepository
				.findByParamName("password");

		if (password != null) {
			data.setPassword(password.getParamValue());
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

		Configuration password = configurationRepository
				.findByParamName("password");

		if (password != null) {
			password.setParamValue(configDataObject.getPassword());
			configurationRepository.save(password);
		} else {
			Configuration c = new Configuration();
			c.setParamName("password");
			c.setParamValue(configDataObject.getPassword());
			configurationRepository.save(c);
		}

		return "redirect:/";
	}

}
