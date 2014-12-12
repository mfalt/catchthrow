package refgen;

public abstract class ReferenceGenerator {

	public static final int POS=0, VEL=1, ANGLE=2, ANGLEVEL=3; 
	protected double ref[] = new double[4];
	
	public abstract double[] getRef();

}













