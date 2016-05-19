package de.hhn.se.embedded.zigbee.raumserver;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device.Type;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDevice;

public class CommandHandler {

	@Autowired
	private TemperatureController temperatureController;

	@Autowired
	private DeviceRepository deviceRepository;

	@Value("${roomserver.id}")
	String roomId;

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private ZigBeeDevice zigBeeDevice;

	// public void handleMessage(String message) {
	// System.out.println("Received <" + message + ">");
	// try {
	// Command command = objectMapper.readValue(message, Command.class);
	//
	// if(command.getTargetDevice().equalsIgnoreCase(roomId+"_HEATING")){
	// if(command.getType().equals(CommandType.SET.name())){
	// this.temperatureController.setNewTargetTemperature(command.getValue());
	// }
	// }
	//
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	//
	//
	// }

	public void handleMessage(String message) {
		System.out.println("Received <" + message + ">");
		try {
			Device device = objectMapper.readValue(message, Device.class);

			if (device.getType().equals(Type.HEATING.name())) {
				this.temperatureController.setNewTargetTemperature(device
						.getTargetValue());
			} else {
				Device fromDb = this.deviceRepository.findOne(device
						.getDeviceId());
				if (fromDb != null) {

					Integer val = new Integer(device.getTargetValue()
							.intValue());

					this.zigBeeDevice.sendValue(fromDb.getZigBeeAddress(),
							val.byteValue());
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}