package de.hhn.se.embedded.zigbee.raumserver;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

public class DummyTemperatureController implements TemperatureController {
	
	@Autowired
	private TemperatureSensor tempSensor;

	private float targetTemperature = TemperatureController.DEFAULT_TEMPERATURE;

	@Override
	public void setNewTargetTemperature(float temp) {
		this.targetTemperature = temp;

	}

	@PostConstruct
	private void doTempControlling() {
		(new Thread(new MyRunnable())).start();
	}

	class MyRunnable implements Runnable {

		public void run() {
			while(true){
				
				System.out.println("current temperature is "+ tempSensor.getValue() + ". Target is " + targetTemperature);
				
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
