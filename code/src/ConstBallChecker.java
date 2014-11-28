
public class ConstBallChecker implements StateChecker {
	double yLast;
	int count = 0;
	final double TOL = 0.01;
	final int SAMPLES = 5;
	
	//y = beamangle,ballpos
	@Override
	public boolean check(double[] y) {
		if(Math.abs(y[1]-yLast)<TOL){//TODO: Find good tolerance
			yLast = y[1];
			count++;
			if(count>SAMPLES){ //TODO: find good amount of samples
				return true;
			} else {
				return false;
			}
		} else {
			yLast = y[1];
			count = 0;
			return false;
		}
	}
}
