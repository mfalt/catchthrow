package checker;

import refgen.ReferenceGenerator;

import com.sun.org.apache.xml.internal.security.encryption.Reference;

public class ConstBeamChecker implements StateChecker {
	double angleRef;
	int count = 0;
	final double TOL = 0.01;
	final int SAMPLES = 5;

	// y = beamangle,ballpos
	// @Override
	public boolean check(double[] measurement) {
		if (Math.abs(measurement[0] - angleRef) < TOL) {// TODO: Find good tolerance
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

	public void setValue(double angleRef) {
		this.angleRef = angleRef;
	}

	@Override
	public void reset() {
		count = 0;
		
	}
}