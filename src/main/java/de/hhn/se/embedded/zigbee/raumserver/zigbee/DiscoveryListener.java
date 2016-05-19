package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.listeners.IDiscoveryListener;


public class DiscoveryListener implements IDiscoveryListener {
	private static final String PARAM_NODE_ID = "NI";
	private static final String PARAM_VALUE_NODE_ID = "SWITCH";

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
		
	

		try {
			System.out.println("type param is "
					+ discoveredDevice.getParameter("DD"));
			 discoveredDevice.setParameter(PARAM_NODE_ID,
			 PARAM_VALUE_NODE_ID.getBytes());
			 discoveredDevice.writeChanges();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(discoveredDevice.getNodeID());

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
		if (error == null)
			System.out.println(">> Discovery process finished successfully.");
		else
			System.out
					.println(">> Discovery process finished due to the following error: "
							+ error);

		System.exit(0);
	}
}