
public class Monitor {
	
	private BeamRegul beamRegul;
	private BeamBallRegul beamBallRegul;
	private BeamBallRegul beamBallThrowSmall;
	private BeamBallRegul beamBallThrowMedium;
	private BeamBallRegul beamBallThrowLarge;
	private Regul currentRegul;
	private long t;
	private static final int OFF=0, BEAM=1, BALL=2;
	private int mode;
	private double[] latestBeamAngles;
	private int nextWrite, size;
	private double uPos, uAngle;
	
	/** Constructor*/
	public Monitor() {
		mode = OFF; //mode is the state of our program
		beamRegul = new BeamRegul();
		beamBallRegul = new BeamBallRegul();
		size = 3;
		latestBeamAngles = new double[size];
	}
	
	private void setCurrentRegul() {
		
		switch(mode) {
		case 0: {
			currentRegul = null;
			break;
		}
		case 1: {
			currentRegul = beamRegul;
			break;
		}
		case 2: {
			currentRegul = beamBallRegul;
			break;
		}
		}
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
	public synchronized void setMode(int m){
		mode = m;
		setCurrentRegul(); //update currentRegul
	}
	
	/** called by SwitchThread and Opcom*/
	public synchronized int getMode() {
		return mode;
	}
	
}	