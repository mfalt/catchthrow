package refgen;


public class ConstantRef extends ReferenceGenerator {
	
	private int actualState;
	
	public ConstantRef(int state) {
		actualState = state; 
	}
	
	public double[] getRef() {
		return ref;
	}
	
	public void setRef(double r) {
		ref[actualState] = r;
	}

}
