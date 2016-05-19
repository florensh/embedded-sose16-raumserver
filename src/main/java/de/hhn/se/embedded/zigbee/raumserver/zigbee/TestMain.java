package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import gnu.io.CommPortIdentifier;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.models.XBee64BitAddress;

public class TestMain {

	/* Constants */

	// TODO Replace with the serial port where your module is connected to.
	private static final String PORT = "/dev/ttyS80";
	// TODO Replace with the baud rate of your module.
	private static final int BAUD_RATE = 9600;
	private static final String PARAM_NODE_ID = "NI";
	private static final String PARAM_VALUE_NODE_ID = "SWITCH";

	static void listPorts() {
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier
				.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier portIdentifier = portEnum.nextElement();
			System.out.println(portIdentifier.getName());
		}
	}

	/**
	 * Application main method.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {

		listPorts();

		// System.setProperty("java.library.path", "./");
		String libPathProperty = System.getProperty("java.library.path");
		System.out.println(libPathProperty);

		System.out.println(" +-------------------------------------------+");
		System.out.println(" | XBee Java Library Discover Devices Sample |");
		System.out.println(" +-------------------------------------------+\n");

		XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);

		try {
			myDevice.open();
			myDevice.setReceiveTimeout(10000);

			System.out.println("api mode is " + myDevice.getOperatingMode());

			XBeeNetwork myXBeeNetwork = myDevice.getNetwork();
			// System.out.println(myDevice.get);

			// myXBeeNetwork.setDiscoveryTimeout(15000);
			// myDevice.setParameter("NT", ByteUtils.longToByteArray(15000 /
			// 100));
			// myDevice.writeChanges();

			// myXBeeNetwork.addDiscoveryListener(new DiscoveryListener());
			//
			 myXBeeNetwork.startDiscoveryProcess();
			 
			 while(myXBeeNetwork.isDiscoveryRunning()){
				 System.out.print(".");
			 }
			 System.out.println("weiter");
			RemoteXBeeDevice remoteDevice = myXBeeNetwork.getDevice(new XBee64BitAddress("0013A20040AFBDDE"));
			System.out.println("device type is "
					+ remoteDevice.getNodeID());
			
//			remoteDevice.setParameter(PARAM_NODE_ID,
//			 PARAM_VALUE_NODE_ID.getBytes());
//			remoteDevice.writeChanges();
			
	

			// byte[] test = "3".getBytes();
			// myDevice.sendBroadcastData(test);
			// RemoteXBeeDevice remote =
			// myXBeeNetwork.getDevice("0013A20040AFBDDE");
			// myDevice.sendData(remote, test);

			System.out.println("\n>> Discovering remote XBee devices...");
			myDevice.close();

		} catch (Exception e) {
			e.printStackTrace();
			myDevice.close();
			System.exit(1);
		}
	}

}