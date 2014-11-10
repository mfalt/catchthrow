
public class BeamBallRegul extends Regul {

	private PIDParameters p;
	private BeamRegul inner;
	private double P, I, D;
	private double ad, bd, bi, ar;
	private double uPos, uAngle, y, yref, yold;
	private double[] sendToInner;
	
	/** Constructor */
	public BeamBallRegul(BeamRegul in){
		inner = in;
		p = new PIDParameters();
		inner = new BeamRegul();
		p.K = -0.1;
		p.Ti = 0.0;
		p.Tr = 0.0;
		p.Td = 1.0;
		p.N = 6.0;
		p.Beta = 1.0;
		p.integratorOn = false;
		setParameters(p);
		I = 0;
		D = 0;
		yold = 0;
		sendToInner = new double[2];
	}
	
	/** calculates the control signal
	 *  as for now it returns a double, to be changed
	 *  to an OutSignal object if needed when we do
	 *  more advanced stuffs
	 */
	public double calculateOutput(double[] yy, double yref, double h) {
		y = yy[1];
		this.yref = yref;
		ad = p.Td/(p.Td + p.N*h);
		bd = p.K*p.N*ad;
		P = p.K*(p.Beta*yref - y);
		D = ad*D - bd*(y - yold);
		uPos = P + I + D;
		sendToInner[0] = yy[0];
		sendToInner[1] = 0;
		uAngle = inner.calculateOutput(sendToInner, uPos, h);
		inner.updateState(uAngle, h);
		return uAngle;
	}

	/** updates the controller state
	 *  uses tracking-based anti-windup
	 */
	public void updateState(double u, double h) {
		if(p.integratorOn) {
			bi = p.K*h/p.Ti;
			ar = h/p.Tr;
			I = I + bi*(yref - y) + ar*(u - uPos); 
			} else {
				I = 0.0;
			}
		yold = y;
	}
	
	public void setParameters(PIDParameters newParameters) {
		p = (PIDParameters)newParameters.clone();
		if (!p.integratorOn) {
			I = 0.0;
		}
	}
	
	public PIDParameters getParameters() {
		return p;
	}
	
}


