package checker;

public class ConstBallChecker implements StateChecker {
	double positionRef;
	int count = 0;
	final double TOL = 0.05; // Stiction makes small tolerances hard, currently set to 1 dm
	final int SAMPLES = 200;

	// y = beamangle,ballpos
	// @Override
	public boolean check(double[] measurement) {
		if (Math.abs(measurement[1] - positionRef) < TOL) {// TODO: Find good tolerance
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

	public void setValue(double y) {
		positionRef = y;
	}

	@Override
	public void reset() {
		count = 0;
		
	}
}