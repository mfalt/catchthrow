
public class ConstantRef extends ScalarRef {
	
	public double[] getRef() {
		return ref;
	}
	
	public void setRef(double r) {
		ref[actualState] = r;
	}

}
