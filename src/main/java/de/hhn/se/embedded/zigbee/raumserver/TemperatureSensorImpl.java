package de.hhn.se.embedded.zigbee.raumserver;

import java.io.IOException;
import java.util.Observable;

import javax.annotation.PostConstruct;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.system.SystemInfo;

public class TemperatureSensorImpl extends Observable implements
		TemperatureSensor {

	private static final float TEMPERATURE_CORRECTION = - 4.0f;
	private I2CBus bus = null;
	private I2CDevice device = null;

	public TemperatureSensorImpl() throws IOException, InterruptedException {
		int sensorAddress = 0x48;

		if (SystemInfo.getBoardType().equals(SystemInfo.BoardType.ModelA_Rev1)) {
			bus = I2CFactory.getInstance(I2CBus.BUS_0);
		} else {
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
		}

		if (bus != null) {
			device = bus.getDevice(sensorAddress);
		}
	}

	public double getTemperature() throws IOException {
		return getReadingFromDevice() + TEMPERATURE_CORRECTION;
	}

	private void close() throws IOException {

		if (bus != null) {
			bus.close();
		}
	}

	@PostConstruct
	private void startReadThread() {
		Thread t = new Thread(new SensorReader());
		t.setDaemon(true);
		t.start();

	}

	class SensorReader implements Runnable {

		@Override
		public void run() {

			while (true) {

				try {
					double temp = getTemperature();
					setTemp((float) temp);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}

	}

	public float convertBitsToCelsiusFloat(final byte[] bytes) {

		float retVal = 0f;

		if (bytes[1] >= 0) {
			// this is the case if the byte indicating the "0,5°C" part was set
			// to 0 = no 1/2 degree was measured.
			retVal = bytes[0];
		} else {
			// the very first bit of the "0,5°C-indicator byte" was set to "1",
			// so that this byte has a negative value --> a 1/2 degree was
			// measured.
			retVal = bytes[0];
			retVal = retVal + (0.5f);
		}
		return retVal;
	}

	private float getReadingFromDevice() throws IOException {

		float currentTemp = 0f;

		if (device != null) {

			byte[] tempBuffer = new byte[2];
			int bytesRead = device.read(tempBuffer, 0, 2);

			currentTemp = convertBitsToCelsiusFloat(tempBuffer);

		}

		return currentTemp;
	}

	private Float temp;

	public Float getTemp() {
		return temp;
	}

	public void setTemp(Float temp) {
		if (temp != null && !temp.equals(this.temp)) {
			Float oldVal = this.temp;
			this.temp = temp;
			setChanged();
			notifyObservers(oldVal);
		}
	}

}