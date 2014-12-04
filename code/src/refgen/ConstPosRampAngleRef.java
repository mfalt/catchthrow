package refgen;

public class ConstPosRampAngleRef extends ReferenceGenerator {
	
	private int constState = ReferenceGenerator.POS;
	private int rampState = ReferenceGenerator.ANGLE;
	private double angleRampSlope = 0.0;
	private long tBefore, tNow; //tNow is current time, tLast is what the time was at the sample before
	
	public ConstPosRampAngleRef() {
		
	}
	
	//@Override
	public double[] getRef() {
		tNow = System.currentTimeMillis();
		ref[rampState] = ref[rampState] + angleRampSlope*(tNow - tBefore) * 0.001;
		tBefore = tNow;
		return ref;
	}

	public void setRef(double posRef, double angleRampSlope) {
		ref[constState] = posRef;
		this.angleRampSlope = angleRampSlope;
		tBefore = System.currentTimeMillis();
	}

	public void setInitialRef(double initRef) {
		ref[rampState] = initRef;
	}
	
}
