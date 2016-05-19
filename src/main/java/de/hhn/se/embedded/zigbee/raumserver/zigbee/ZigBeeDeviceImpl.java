package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

import de.hhn.se.embedded.zigbee.raumserver.domain.Device;
import de.hhn.se.embedded.zigbee.raumserver.domain.DeviceRepository;
import de.hhn.se.embedded.zigbee.raumserver.web.UserService;

public class ZigBeeDeviceImpl implements ZigBeeDevice, IDiscoveryListener,
		IDataReceiveListener {

	@Autowired
	DeviceRepository deviceRepository;

	@Autowired
	UserService userService;

	@Value("${roomserver.id}")
	String roomId;

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
			Thread t = new Thread(new MyRunnable());
			t.setDaemon(true);
			t.start();
		} catch (XBeeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			myDevice.close();
		}

	}

	class MyRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					myXBeeNetwork.startDiscoveryProcess();
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

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
		System.out.format(">> Device discovered: %s%n",
				discoveredDevice.toString());

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
			System.out.println(">> Discovery process finished successfully.");

			for (RemoteXBeeDevice discoveredDevice : this.myXBeeNetwork
					.getDevices()) {
				String address = discoveredDevice.get64BitAddress().toString();
				Device d = this.deviceRepository.findByZigBeeAddress(address);
				if (d == null) {
					RemoteXBeeDevice remote = this.myXBeeNetwork
							.getDevice(new XBee64BitAddress(address));
					byte[] data = { 2, 0, 0 };
					try {
						this.myDevice.sendData(remote, data);
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
	public void sendValue(String address, byte value) throws TimeoutException,
			XBeeException {
		RemoteXBeeDevice remote = this.myXBeeNetwork
				.getDevice(new XBee64BitAddress(address));
		if (remote != null) {
			byte[] data = { 1, value, 0 };
			this.myDevice.sendData(remote, data);
		}

	}

	@Override
	public void dataReceived(XBeeMessage xbeeMessage) {
		System.out.format("Received data from %s >> %s\n", xbeeMessage
				.getDevice().get64BitAddress(), HexUtils
				.prettyHexString(HexUtils.byteArrayToHexString(xbeeMessage
						.getData())));
		// System.out.println();

		byte[] data = xbeeMessage.getData();
		byte cmd = data[0];
		byte arg1 = data[1];
		byte arg2 = data[2];

		if (cmd == 3) {

			String type = arg1 == 1 ? "THERMOSTAT" : "SWITCH";

			String address = xbeeMessage.getDevice().get64BitAddress()
					.toString();
			Device d = this.deviceRepository.findByZigBeeAddress(address);
			if (d == null) {
				d = new Device();
				UUID deviceId = UUID.randomUUID();
				d.setDeviceId(deviceId.toString());
				d.setZigBeeAddress(address);
				d.setType(type);

				if ("THERMOSTAT".equals(type)) {
					Device heating = new Device();
					heating.setDeviceId(this.roomId + "_HEATING");
					heating.setType("HEATING");
					this.userService.registerDevice(heating);
					this.deviceRepository.save(heating);
				} else {
					this.userService.registerDevice(d);

				}

				this.deviceRepository.save(d);

			}

		} else {
			System.out.println("Unknown cmd!");
		}
	}

}
