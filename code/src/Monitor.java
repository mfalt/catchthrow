
public class Monitor {
	
	private BeamRegul beamRegul;
	private BeamBallRegul beamBallRegul;
	private BeamBallRegul beamBallThrowSmall;
	private BeamBallRegul beamBallThrowMedium;
	private BeamBallRegul beamBallThrowLarge;
	private Regul currentRegul;
	public static final int OFF=0, BEAM=1, BALL=2;
	private int mode;
	private double[] latestBeamAngles;
	private int nextWrite, size;
	private double h = 0.1;
	private double uPos, uAngle;
	
	/** Constructor*/
	public Monitor() {
		mode = OFF; //mode is the state of our program
		beamRegul = new BeamRegul();
		beamBallRegul = new BeamBallRegul(beamRegul);
		size = 3;
		latestBeamAngles = new double[size];
	}
	
	/** called by SwitchThread*/
	public synchronized double[] getLatestBeamAngles() {
		return latestBeamAngles;
	}
	
	/** called by RegulThread*/
	public synchronized double calcOutput(double[] y, double yref) {
		if(currentRegul == null) {
			return 0;
		} else {
			latestBeamAngles[nextWrite] = y[0];
			nextWrite = (nextWrite + 1) % size;
			return currentRegul.calculateOutput(y, yref, h);
		}
	}
	
	/** called by RegulThread*/
	public synchronized void updateState(double u) {
		if(currentRegul != null) {
			currentRegul.updateState(u, h);
		}
	}
	
	/** called by Opcom*/
	public synchronized void setInnerParameters(PIParameters p) {
		beamRegul.setParameters(p);
	}
	
	/** called by Opcom*/
	public synchronized PIParameters getInnerParameters() {
		return beamRegul.getParameters();
	}
	
	/** called by Opcom*/
	public synchronized void setOuterParameters(PIDParameters p) {
		beamBallRegul.setParameters(p);
	}
	
	/** called by Opcom*/
	public synchronized PIDParameters getOuterParameters(){
		return beamBallRegul.getParameters();
	}
	
	/** called by Opcom*/
	public synchronized void setOFFMode(){
		mode = OFF;
		currentRegul = null; //update currentRegul
	}
	
	/** called by Opcom*/
	public synchronized void setBeamMode(){
		mode = BEAM;
		currentRegul = beamRegul; //update currentRegul
	}
	
	/** called by Opcom*/
	public synchronized void setBallMode(){
		mode = BALL;
		currentRegul = beamBallRegul; //update currentRegul
	}
	
	/** called by SwitchThread and Opcom*/
	public synchronized int getMode() {
		return mode;
	}
	
	/** called by RegulThread*/
	public synchronized void setH(double h) {
		this.h = h;
	}
	
	/** called by RegulThread*/
	public synchronized double getH() {
		return h;
	}
	
}	