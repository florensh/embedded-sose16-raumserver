package de.hhn.se.embedded.zigbee.raumserver;

import java.util.Observable;
import java.util.Random;

import javax.annotation.PostConstruct;

public class DummyTemperatureSensor extends Observable implements
		TemperatureSensor {

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

	@PostConstruct
	private void createDummyData() {
		Thread t = new Thread(new DummyDataCreator());
		t.setDaemon(true);
		t.start();

	}

	class DummyDataCreator implements Runnable {

		@Override
		public void run() {

			while (true) {
				float minX = 22.0f;
				float maxX = 23.0f;

				Random rand = new Random();

				float finalX = rand.nextFloat() * (maxX - minX) + minX;
				setTemp(finalX);

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}

	}

}
