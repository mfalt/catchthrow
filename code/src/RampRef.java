
public class RampRef extends ReferenceGenerator {
	
	private double initRef = 0.0;
	private double rampSlope = 0.0;
	
	@Override
	public double getRef() {
		return initRef + rampSlope*getTimeSeconds();
	}

	public void setVelocity(double rampSlope) {
		this.rampSlope = rampSlope;
	}

	public void setInitialRef(double initRef) {
		this.initRef = initRef;
	}

}
