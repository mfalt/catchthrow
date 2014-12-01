public class ConstBeamChecker implements StateChecker {
	double yRef;
	int count = 0;
	final double TOL = 0.1;
	final int SAMPLES = 5;

	// y = beamangle,ballpos
	// @Override
	public boolean check(double[] y) {
		if (Math.abs(y[0] - yRef) < TOL) {// TODO: Find good tolerance
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
		yRef = y;
	}
}