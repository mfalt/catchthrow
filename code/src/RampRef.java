
public class RampRef extends ReferenceGenerator {
	
	private int actualState;
	private double rampSlope = 0.0;
	private long tBefore, tNow; //tNow is current time, tLast is what the time was at the sample before
	
	public RampRef(int state) {
		actualState = state; 
	}
	
	//@Override
	public double[] getRef() {
		tNow = System.currentTimeMillis();
		ref[actualState] = ref[actualState] + rampSlope*(tNow - tBefore) * 0.001;
		tBefore = tNow;
		return ref;
	}

	public void setRef(double rampSlope) {
		this.rampSlope = rampSlope;
		tBefore = System.currentTimeMillis();
	}

	public void setInitialRef(double initRef) {
		ref[actualState] = initRef;
	}
	
}
