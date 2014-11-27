public abstract class ReferenceGenerator {
	
	private long t0 = -1;
	
	/**
	 * References in the order:
	 * (posRef, velRef, angleRef, angleVelRef) 
	 */
	protected double ref[] = new double[4];
	
	public abstract double[] getRef();
	
	public void resetTime() {
		t0 = System.currentTimeMillis();
	}
	
	protected double getTimeSeconds() {
		return (System.currentTimeMillis() - t0) / 1000.0;
	}

}

