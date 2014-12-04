package regul;
import refgen.ReferenceGenerator;
import main.*;

public class BeamRegul extends Regul {
	
	private PIDParameters p;
	private double P;
	private double I;
	private double D;
	private double bi, ar, ad, bd;
	private double v;      //output: desired control signal
	private double angle,angleOld;      //input: measured variable
	private double angleRef;   //input: set point
	
	public BeamRegul() {
		p = new PIDParameters();
		p.K = 3.0 / RegulThread.radiansPerVolt;
		p.Ti = 0.2;
		p.Tr = 10.0;
		p.Td = 0.4;
		p.N = 20.0;
		p.Beta = 1.0;
		p.integratorOn = true;
		setParameters(p);
		I = 0;
		D = 0;
		angleOld = 0;
	}
	
	
	/** anti wind-up */
	private double limit(double v, double min, double max) {
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return v;
	}
	
	
	/** calculates the control signal
	 *  as for now it returns a double, to be changed
	 *  to an OutSignal object if needed when we do
	 *  more advanced stuffs
	 */
	public double calculateOutput(double[] measurement, double[] ref, double h) {
		double angleRef = ref[ReferenceGenerator.ANGLE];
		P = p.K*(p.Beta*angleRef - measurement[0]);
		ad = p.Td/(p.Td + p.N*h);
		bd = p.K*p.N*ad;
		angle = measurement[0];
		D = ad*D - bd*(angle - angleOld);
		
		//System.out.println((y-yold)*10000);
		v = P + I + D;
		this.angleRef = angleRef;
		return limit(v, -10, 10);
//		return 2.0;
	}

	/** updates the controller state
	 *  uses tracking-based anti-windup
	 */
	public void updateState(double h) {
		if(p.integratorOn) {
			bi = p.K*h/p.Ti;
			ar = h/p.Tr;
			I = I + bi*(angleRef - angle) + ar*(limit(v, -10, 10) - v); 
		} else {
			I = 0.0;
		}
		angleOld = angle;
		//System.out.println("P: "+ P + " I: " + I + "D: " + D);
	}

	/** Must clone newParameters because newParameters
	 *  should not be able to be changed by both OpCom
	 *  and this class*/
	public void setParameters(PIDParameters newParameters) {
		p = (PIDParameters)newParameters.clone();
		if (!p.integratorOn) {
			I = 0.0;
		}
	}
	
	public PIDParameters getParameters() {
		return p;
	}

	public void reset(){
		I = 0;
		D = 0;
	}
}
	
