package refgen;

public abstract class ReferenceGenerator {

	public static final int POS=0, VEL=1, ANGLE=2, ANGLEVEL=3, MASS=4, POSINT=5;
	public static final int nbrStates = 6;
	protected double ref[] = new double[nbrStates];
	private long t0 = -1;

	public abstract double[] getRef();

	public void resetTime() {
		t0 = System.currentTimeMillis();
	}

	protected double getTimeSeconds() {
		return (System.currentTimeMillis() - t0) * 0.001;
	}
}













