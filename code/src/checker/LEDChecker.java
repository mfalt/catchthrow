package checker;

import java.io.IOException;

import se.lth.control.realtime.DigitalIn;
import se.lth.control.realtime.DigitalOut;
import se.lth.control.realtime.IOChannelException;

public class LEDChecker implements StateChecker {
	private DigitalIn digitalIn; // sensor light

	public LEDChecker() {
		try {
			digitalIn = new DigitalIn(0);
		} catch (IOChannelException e) {
			e.printStackTrace();
		}
	}

	// @Override
	public boolean check(double[] y) {
		try {
			return digitalIn.get();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}