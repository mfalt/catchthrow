package regul;

import main.*;
import refgen.*;


public class BeamBallRegul extends Regul {

	private PIDParameters p;
	private BeamRegul inner;
	private double P, I, D;
	private double ad, bd, bi, ar;
	private double angleRef, u, pos, posRef, angleFF, posOld;
	private double[] angleRefs = new double[4];
	
	/** Constructor */
	public BeamBallRegul(BeamRegul in){
		inner = in;
		p = new PIDParameters();
		//inner = new BeamRegul();
		p.K = -0.1;
//		p.K = -0.08 * RegulThread.radiansPerVolt / RegulThread.metersPerVolt;
		p.Ti = 3.0;
		p.integratorOn = true;
		p.Td = 2.0;
		p.N = 6.0;
		p.Tr = 10.0;
		p.Beta = 1.0;
		setParameters(p);
		I = 0;
		D = 0;
		posOld = 0;
	}
	
	/** calculates the control signal
	 *  as for now it returns a double, to be changed
	 *  to an OutSignal object if needed when we do
	 *  more advanced stuffs
	 */
	public double calculateOutput(double[] measurement, double[] ref, double h) {
		posRef = ref[ReferenceGenerator.POS];
		angleFF = ref[ReferenceGenerator.ANGLE];
		angleRefs = ref.clone();
		pos = measurement[1];
		ad = p.Td/(p.Td + p.N*h);
		bd = p.K*p.N*ad;
		P = p.K*(p.Beta*posRef - pos);
		D = ad*D - bd*(pos - posOld);
		angleRef = P + I + D + angleFF;
		angleRefs[ReferenceGenerator.ANGLE] = angleRef;
		u = inner.calculateOutput(measurement, angleRefs, h);

		return u;
	}

	/** updates the controller state
	 *  uses tracking-based anti-windup
	 */
	public void updateState(double h) {
		inner.updateState(h);
		if(p.integratorOn) {
			bi = p.K*h/p.Ti;
			I = I + bi*(posRef - pos);
		} else {
			I = 0.0;
		}
		posOld = pos;
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
	
	public void reset(double[] measurement){
		inner.reset(measurement);
		I = 0;
		D = 0;
		posOld = measurement[1];
	}
	
}


