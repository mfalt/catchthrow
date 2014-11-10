
public class Monitor {
	
	private BeamRegul beamRegul;
	private BeamBallRegul beamBallRegul;
	private BeamBallRegul beamBallThrowSmall;
	private BeamBallRegul beamBallThrowMedium;
	private BeamBallRegul beamBallThrowLarge;
	private Regul currentRegul;
	private long t;
	public static final int OFF=0, BEAM=1, BALL=2;
	private int mode;
	private double[] latestBeamAngles;
	private int nextWrite, size;
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
		t = System.currentTimeMillis();
		latestBeamAngles[nextWrite] = y[0];
		nextWrite = (nextWrite + 1) % size;
		return currentRegul.calculateOutput(y, yref);
	}
	
	/** called by RegulThread*/
	public synchronized void updateState(double u) {
		currentRegul.updateState(u);
		notifyAll(); //I am going to sleep now people so you can use the monitor
		
		// sleep
		long duration;
		t = t + currentRegul.getHMillis();
		duration = t - System.currentTimeMillis();
		if (duration > 0) {
			try {
				wait(duration);  
			} catch (InterruptedException x) {
			}
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
	
}	