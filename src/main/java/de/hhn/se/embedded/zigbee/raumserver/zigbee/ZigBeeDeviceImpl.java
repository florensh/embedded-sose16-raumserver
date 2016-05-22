package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.HexUtils;

import de.hhn.se.embedded.zigbee.raumserver.TemperatureController;
import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;
import de.hhn.se.embedded.zigbee.raumserver.web.UserService;

public class ZigBeeDeviceImpl implements ZigBeeDevice, IDiscoveryListener,
		IDataReceiveListener {

	@Autowired
	DeviceRepository deviceRepository;

	@Autowired
	DeviceService deviceService;

	@Autowired
	UserService userService;

	@Value("${roomserver.id}")
	String roomId;

	private final Logger LOGGER = LoggerFactory.getLogger(ZigBeeDevice.class);

	private static final String PORT = "/dev/ttyS80";
	// private static final String PORT = "/dev/ttyACM0";
	private static final int BAUD_RATE = 9600;

	XBeeDevice myDevice;
	XBeeNetwork myXBeeNetwork;

	@PostConstruct
	private void init() {
		myDevice = new XBeeDevice(PORT, BAUD_RATE);
		try {
			myDevice.open();
			myDevice.setReceiveTimeout(15000);
			myXBeeNetwork = myDevice.getNetwork();
			myXBeeNetwork.addDiscoveryListener(this);
			myDevice.addDataListener(this);

		} catch (XBeeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			myDevice.close();
		}

	}

	@Scheduled(fixedDelay = 120000)
	private void runDiscovery() {
		myXBeeNetwork.startDiscoveryProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.digi.xbee.api.listeners.IDiscoveryListener#deviceDiscovered(com.digi
	 * .xbee.api.RemoteXBeeDevice)
	 */
	@Override
	public void deviceDiscovered(RemoteXBeeDevice discoveredDevice) {
		LOGGER.info("Device discovered: " + discoveredDevice.toString());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.digi.xbee.api.listeners.IDiscoveryListener#discoveryError(java.lang
	 * .String)
	 */
	@Override
	public void discoveryError(String error) {
		System.out.println(">> There was an error discovering devices: "
				+ error);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.digi.xbee.api.listeners.IDiscoveryListener#discoveryFinished(java
	 * .lang.String)
	 */
	@Override
	public void discoveryFinished(String error) {
		if (error == null) {
			LOGGER.info("Discovery process finished successfully - "
					+ this.myXBeeNetwork.getDevices().size()
					+ " devices in network");

			for (RemoteXBeeDevice discoveredDevice : this.myXBeeNetwork
					.getDevices()) {
				String address = discoveredDevice.get64BitAddress().toString();
				Device d = this.deviceRepository.findByZigBeeAddress(address);
				if (d == null) {
					LOGGER.info("Sending status request to " + address);
					RemoteXBeeDevice remote = this.myXBeeNetwork
							.getDevice(new XBee64BitAddress(address));
					byte[] data = { 2, 0, 0 };
					try {
						this.sendMessage(remote, data);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

		} else {
			System.out
					.println(">> Discovery process finished due to the following error: "
							+ error);
		}

	}

	@Override
	public void sendValue(String address, byte value) {
		RemoteXBeeDevice remote = this.myXBeeNetwork
				.getDevice(new XBee64BitAddress(address));
		if (remote != null) {
			byte[] data = { 1, value, 0 };
			this.sendMessage(remote, data);

		}
	}

	private void sendMessage(RemoteXBeeDevice remote, byte[] data) {
		LOGGER.info("sending message to " + remote.get64BitAddress().toString()
				+ " >> " + HexUtils.prettyHexString(data));
		try {
			this.myDevice.sendData(remote, data);
		} catch (XBeeException e) {
			this.LOGGER.error("Error while sending message", e);
		}

	}

	@Override
	public void dataReceived(XBeeMessage xbeeMessage) {

		byte[] data = xbeeMessage.getData();
		byte cmd = data[0];
		byte arg1 = data[1];
		byte arg2 = data[2];
		String type = arg1 == 1 ? "THERMOSTAT" : "SWITCH";
		LOGGER.info("Received status from "
				+ xbeeMessage.getDevice().get64BitAddress() + ": [" + type
				+ " " + (float) arg2 + "]");

		if (cmd == 3) {

			String address = xbeeMessage.getDevice().get64BitAddress()
					.toString();

			Device d = this.deviceRepository.findByZigBeeAddress(address);
			if (d == null) {
				LOGGER.info(xbeeMessage.getDevice().get64BitAddress()
						+ " is unknown, registering to backend");
				d = new Device();
				UUID deviceId = UUID.randomUUID();
				d.setDeviceId(deviceId.toString());
				d.setZigBeeAddress(address);
				d.setType(type);

				if ("THERMOSTAT".equals(type)) {
					Device heating = new Device();
					heating.setDeviceId(this.roomId + "_HEATING");
					heating.setType("HEATING");
					heating.setTargetValue(TemperatureController.DEFAULT_TEMPERATURE);
					this.registerDevice(heating);

					this.deviceRepository.save(heating);
				} else {
					d.setTargetValueOnDevice((float) arg2);
					d.setValue((float) arg2);
					this.registerDevice(d);
				}

				this.deviceRepository.save(d);

			} else {
				if (!"THERMOSTAT".equals(type)) {
					d.setTargetValueOnDevice((float) arg2);
					this.updateDevice(d);
				}

			}

		} else {
			System.out.println("Unknown cmd!");
		}
	}

	@Async
	private void registerDevice(Device d) {
		this.userService.registerDevice(d);

	}

	@Async
	private void updateDevice(Device d) {
		this.deviceService.updateDevice(d);

	}

}
