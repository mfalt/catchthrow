
public class BeamRegul extends Regul {
	
	private PIDParameters p;
	private double P;
	private double I;
	private double D;
	private double bi, ar, ad, bd;
	private double v;      //output: desired control signal
	private double y,yold;      //input: measured variable
	private double yref;   //input: set point
	
	public BeamRegul() {
		p = new PIDParameters();
		p.K = 3.0;
		p.Ti = 2.0;
		p.Tr = 10.0;
		p.Td = 0.3;
		p.N = 20.0;
		p.Beta = 1.0;
		p.integratorOn = true;
		setParameters(p);
		I = 0;
		D = 0;
		yold = 0;
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
	public double calculateOutput(double[] yy, double yref, double h) {
		P = p.K*(p.Beta*yref - yy[0]);
		ad = p.Td/(p.Td + p.N*h);
		bd = p.K*p.N*ad;
		y = yy[0];
		D = ad*D - bd*(y - yold);
		
		//System.out.println((y-yold)*10000);
		v = P + I + D;
		this.yref = yref;
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
			I = I + bi*(yref - y) + ar*(limit(v, -10, 10) - v); 
		} else {
			I = 0.0;
		}
		yold = y;
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
	
