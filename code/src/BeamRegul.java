
public class BeamRegul extends Regul {
	
	private PIParameters p;
	private double P;
	private double I;
	private double bi, ar;
	private double v;      //output: desired control signal
	private double y;      //input: measured variable
	private double yref;   //input: set point
	
	public BeamRegul() {
		p = new PIParameters();
		p.K = 1.0;
		p.Ti = 0.0;
		p.Tr = 0.0;
		p.Beta = 1.0;
		p.H = 0.1;
		p.integratorOn = false;
		setParameters(p);
		I = 0;
	}
	
	
	/** calculates the control signal
	 *  as for now it returns a double, to be changed
	 *  to an OutSignal object if needed when we do
	 *  more advanced stuffs
	 */
	public double calculateOutput(double[] yy, double yref) {
		P = p.K*(p.Beta*yref - yy[0]);
		v = P + I;
		y = yy[0];
		this.yref = yref;
		return v;
	}

	/** updates the controller state
	 *  uses tracking-based anti-windup
	 */
	public void updateState(double u) {
		if(p.integratorOn) {
			bi = p.K*p.H/p.Ti;
			ar = p.H/p.Tr;
			I = I + bi*(yref - y) + ar*(u - v); 
			} else {
				I = 0.0;
			}
	}
	
	/** Must clone newParameters because newParameters
	 *  should not be able to be changed by both OpCom
	 *  and this class*/
	public void setParameters(PIParameters newParameters) {
		p = (PIParameters)newParameters.clone();
		if (!p.integratorOn) {
			I = 0.0;
		}
	}
	
	public PIParameters getParameters() {
		return p;
	}

	/** returns this controller's sampling interval*/
	public long getHMillis() {
		return (long) (p.H*1000.0);
	}

}
	
