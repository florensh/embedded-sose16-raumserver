package de.hhn.se.embedded.zigbee.raumserver;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;


public class CommandHandler {
	
	@Autowired
	private TemperatureController temperatureController;
	
	ObjectMapper objectMapper = new ObjectMapper();


	public void handleMessage(String message) {
		System.out.println("Received <" + message + ">");
		try {
			Command command = objectMapper.readValue(message, Command.class);
			
			if(command.getTargetDevice().equalsIgnoreCase("heating")){
				if(command.getType().equals("S")){
					this.temperatureController.setNewTargetTemperature(command.getValue());
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}


}