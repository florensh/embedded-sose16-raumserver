package de.hhn.se.embedded.zigbee.raumserver;

import org.springframework.stereotype.Component;

import com.pi4j.io.gpio.PinState;

@Component
public class LEDController {


	public void startFlashingFast() {
		Application.ledFuture.cancel(true);
		Application.led.setState(PinState.HIGH);
		Application.ledFuture = Application.led.blink(50, 1000, PinState.HIGH);

		if (Application.ledFuture.isDone()) {
			Application.led.setState(PinState.HIGH);
		}
	}

	public void stop() {
		if (Application.ledFuture != null) {
			Application.ledFuture.cancel(true);
		}
	}

}
