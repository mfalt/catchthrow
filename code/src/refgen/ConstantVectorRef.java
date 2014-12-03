package refgen;

import java.util.Arrays;

public class ConstantVectorRef extends ReferenceGenerator {
	
	public double[] getRef() {
		return ref;
	}
	
	public void setPosRef(double r) {
		ref[ReferenceGenerator.POS] = r;
	}

	public void setVelRef(double r) {
		ref[ReferenceGenerator.VEL] = r;
	}

	public void setAngleRef(double r) {
		ref[ReferenceGenerator.ANGLE] = r;
	}

	public void setAngleVelRef(double r) {
		ref[ReferenceGenerator.ANGLEVEL] = r;
	}
	
	/**
	 * Sets references of all states to zero.
	 */
	public void setZeroRef() {
		Arrays.fill(ref, 0.0);
	}

}
