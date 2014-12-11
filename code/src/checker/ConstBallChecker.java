package checker;

public class ConstBallChecker implements StateChecker {
	double positionRef;
	public static final double DEFAULT_TOL = 0.03;
	int count = 0;
	double tol = DEFAULT_TOL; // Stiction makes small tolerances hard, currently set to 1 dm
	final int SAMPLES = 400;

	// y = beamangle,ballpos
	// @Override
	public boolean check(double[] measurement) {
		if (Math.abs(measurement[1] - positionRef) < tol) {// TODO: Find good tolerance
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

	public void setValue(double y, double tolerance) {
		positionRef = y;
		tol = tolerance;
	}

	@Override
	public void reset() {
		count = 0;
		
	}
}