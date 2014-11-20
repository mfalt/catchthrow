public abstract class ReferenceGenerator {
	private long t0 = -1;
	
	public abstract double getRef();
	
	public void resetTime() {
		t0 = System.currentTimeMillis();
	}
	
	protected double getTimeSeconds() {
		return (System.currentTimeMillis() - t0) / 1000.0;
	}

}

