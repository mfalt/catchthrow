
public class RampRef extends ScalarRef {
	
	private double initRef = 0.0;
	private double rampSlope = 0.0;
	
	@Override
	public double[] getRef() {
		ref[actualState] = initRef + rampSlope*getTimeSeconds();
		return ref;
	}

	public void setRampSlope(double rampSlope) {
		this.rampSlope = rampSlope;
	}

	public void setInitialRef(double initRef) {
		this.initRef = initRef;
	}
	
}
