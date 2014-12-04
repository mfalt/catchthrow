package regul;

/**
 * LQG control for control to a specific state, around which the model is linearized
 */
public class TimeInvLQG extends Regul {
	double linPoint[];
	
	public TimeInvLQG(double[] linPoint, double h) {
		this.linPoint = linPoint;
	}
	
	/**
	 * If only controlling to the predefined linearization point with a predefined sample period is allowed,
	 * the ref and h arguments are unnecessary here.
	 */
	public double calculateOutput(double[] measurement, double[] ref, double h) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void updateState(double h) {
		// TODO Auto-generated method stub

	}

}
