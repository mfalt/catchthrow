
public class RampRef extends ReferenceGenerator {
	
	private double initRef = 0.0;
	private double velocity = 0.0;
	
	@Override
	public double getRef() {
		return initRef + velocity*getTimeSeconds();
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public void setInitialRef(double initRef) {
		this.initRef = initRef;
	}

}
