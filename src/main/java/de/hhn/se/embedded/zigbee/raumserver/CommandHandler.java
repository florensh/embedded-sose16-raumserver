package de.hhn.se.embedded.zigbee.raumserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device.Type;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.DeviceService;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDevice;

public class CommandHandler {

	@Autowired
	private TemperatureController temperatureController;

	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	LEDController ledController;

	@Autowired
	private DeviceService deviceService;

	@Value("${roomserver.id}")
	String roomId;

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private ZigBeeDevice zigBeeDevice;

	private final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

	public void handleMessage(String message) {
		
		this.ledController.startFlashingFast();
		
		try {
			Device device = objectMapper.readValue(message, Device.class);
			String logMsg = "Received device update [id: "
					+ device.getDeviceId() + " / target value: "
					+ device.getTargetValue() + "]";
			LOGGER.info(logMsg);

			if (device.getType().equals(Type.HEATING.name())) {
				this.temperatureController.setNewTargetTemperature(device
						.getTargetValue());

				// ACK to Backend!
				Device toSend = new Device();
				toSend.setDeviceId(device.getDeviceId());
				toSend.setTargetValueOnDevice(device.getTargetValue());
				this.deviceService.updateDevice(toSend);

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