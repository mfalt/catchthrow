public abstract class ReferenceGenerator {
	
	public static final int POS=0, VEL=1, ANGLE=3, ANGLEVEL=4; 
	private long t0 = -1;
	protected double ref[] = new double[4];
	
	public abstract double[] getRef();
	
	public void resetTime() {
		t0 = System.currentTimeMillis();
	}
	
	protected double getTimeSeconds() {
		return (System.currentTimeMillis() - t0) * 0.001;
	}

}

