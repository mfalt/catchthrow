package refgen;


public class RampToRef extends ReferenceGenerator {
	
	private int actualState;
	private double speed = 0.0;
	private double finalRef = 0.0;
	private double slopeSign;
	private long tBefore, tNow; //tNow is current time, tLast is what the time was at the sample before
	
	public RampToRef(int state) {
		actualState = state; 
	}
	
	//@Override
	public double[] getRef() {
		tNow = System.currentTimeMillis();
		ref[actualState] = ref[actualState] + slopeSign*speed*(tNow - tBefore) * 0.001;
		
		if(slopeSign > 0)
			ref[actualState] = Math.min(ref[actualState], finalRef);
		else
			ref[actualState] = Math.max(ref[actualState], finalRef);
		
		tBefore = tNow;
		return ref;
	}

	public void setRef(double speed, double finalRef) {
		this.speed = Math.abs(speed);
		this.finalRef = finalRef;
		slopeSign = Math.signum(finalRef - ref[actualState]);
		tBefore = System.currentTimeMillis();
	}

	public void setInitialRef(double initRef) {
		ref[actualState] = initRef;
	}
	
}


