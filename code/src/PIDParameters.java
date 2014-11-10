
public class PIDParameters implements Cloneable {
	double K;
	double Ti;
	double Tr;
	double Td;
	double N;
	double Beta;
	boolean integratorOn;
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
