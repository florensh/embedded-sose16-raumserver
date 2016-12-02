package de.hhn.se.embedded.zigbee.raumserver;

import java.util.Observable;
import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;

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



	@Scheduled(fixedDelay = 5000)
	private void readTemp() {
		double temp;
		float minX = 18.0f;
		float maxX = 19.0f;

		Random rand = new Random();

		float finalX = rand.nextFloat() * (maxX - minX) + minX;
		setTemp(finalX);

	}



}
