package checker;

import main.RegulThread;

public class BallOnBeamChecker implements StateChecker {
	final double TOL = 0.05; //Wait until ball is at least 5 cm from the left edge
	double positionRef = RegulThread.posMin + TOL;
	int count = 0;
	final int SAMPLES = 5;

	// y = beamangle,ballpos
	// @Override
	public boolean check(double[] measurement) {
		if (measurement[1] > positionRef + TOL) {
			count++;
			if (count > SAMPLES) { // TODO: find good amount of samples
				return true;
			} else {
				return false;
			}
		} else {
			count = 0;
			return false;
		}
	}

}