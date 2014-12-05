package regul;

/**
 * LQG control for control to a specific state, around which the model is linearized
 */
public class TimeInvLQG extends Regul {
	double linPoint[];
	
	private int N; // Number of states
	private double Phi[][]; // NxN
	private double Gamma[]; // Nx1 (one input)
	private double C[][]; // 2xN
	private double D[]; // 2x1
	
	private double L[]; // Nx1
	private double K[][]; // Nx2
	
	// Signals
	private double u;
	private double y[]; // 2x1
	private double xhat[]; // Nx1
	
	/**
	 * 
	 * @param linPoint
	 */
	public TimeInvLQG(double[] linPoint) {
		this.linPoint = linPoint;
		
		// Hard coded variables, could be read from .mat file instead.
//		nbrStates = ;
//		Phi[][] = ; // For all elements
//		Gamma[] = ; // For all elements
//		Gamma[] = ; // For all elements
//		D = ;
//		L[] = ; // For all elements
//		K[] = ; // For all elements
//		xhat[] = ; // For all elements
	}
	
	/**
	 * If only controlling to the predefined linearization point with a predefined sample period is allowed,
	 * the ref and h arguments are unnecessary here.
	 */
	public double calculateOutput(double[] measurement, double[] ref, double h) {
		this.y = measurement.clone(); // Clone really necessary?
		u = 0;
		for(int i = 0; i < N; ++i) {
			u -= L[i]*xhat[i];
		}
		return u;
	}

	public void updateState(double h) {
		// Compute residuals
		double epsilon[] = y.clone(); // Clone necessary?
		for(int i = 0; i < N; ++i) {
			epsilon[0] -= C[0][i]*xhat[i]; // Residual of first measurement 
			epsilon[1] -= C[1][i]*xhat[i]; // Residual of second measurement
		}
		
		// Update states
		// xhat = Phi*oldxhat + Gamma*u + K*epsilon
		double oldxhat[] = xhat.clone(); // Clone necessary?
		for(int i = 0; i < N; ++i) { // Set xhat to 0 before adding other stuff
			xhat[i] = 0.0;
		}
		for(int i = 0; i < N; ++i) {
			// First term, Phi*oldxhat
			for(int j = 0; j < N; ++j) {
				xhat[i] += Phi[i][j]*oldxhat[j];
			}
			
			// Second term, Gamma*u
			xhat[i] += Gamma[i]*u;
			
			// Third term, K*epsilon
			for(int j = 0; j < 2; ++j) { // two outputs
				xhat[i] += K[i][j]*epsilon[j];
			}
		}
	}

}
