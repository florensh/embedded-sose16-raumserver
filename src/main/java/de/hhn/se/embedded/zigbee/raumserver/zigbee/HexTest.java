package de.hhn.se.embedded.zigbee.raumserver.zigbee;

import com.digi.xbee.api.utils.HexUtils;

public class HexTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		byte[] i = {1,2};
		byte b = 2;
		System.out.println(HexUtils.byteArrayToHexString("hallo".getBytes()));

	}

}
