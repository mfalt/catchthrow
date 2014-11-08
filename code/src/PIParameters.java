public class PIParameters implements Cloneable {
	double K;
	double Ti;
	double Tr;
	double Beta;
	double H;
	boolean integratorOn;
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
