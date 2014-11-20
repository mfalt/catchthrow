
public class Monitor {
	
	private BeamRegul beamRegul;
	private BeamBallRegul beamBallRegul;
//	private BeamBallRegul beamBallThrowSmall;
//	private BeamBallRegul beamBallThrowMedium;
//	private BeamBallRegul beamBallThrowLarge;
	private Regul currentRegul;
	
	private RefGenGUI refGenGUI;
	private ConstantRef constantRef;
	private RampRef pickupSearchRef;
	private TrajectoryRef throwRefSmall;
	private TrajectoryRef throwRefMedium;
	private TrajectoryRef throwRefLarge;
	private ReferenceGenerator currentRefGen;
	
	public static final int OFF=0, BEAM=1, BALL=2, SEQUENCE=3;
	private int mode;
	private double h = 0.02;
	private boolean LED = false;
	
	/** Constructor*/
	public Monitor() {
		mode = OFF; //mode is the state of our program
		beamRegul = new BeamRegul();
		beamBallRegul = new BeamBallRegul(beamRegul);
		
		constantRef = new ConstantRef();
		pickupSearchRef = new RampRef();
	}
	
	/** called from Main */
	public synchronized void setRefGenGUI(RefGenGUI referenceGenerator){
		refGenGUI = referenceGenerator;
		currentRefGen = refGenGUI;
	}
	
	/** called from SwitchThread */
	public synchronized void setRefGenConstant(double r){
		constantRef.setRef(r);
		currentRefGen = constantRef;
	}
	
	/** called from SwitchThread */
	public synchronized void setRefGenRamp(double velocity){
		pickupSearchRef.setVelocity(velocity);
		pickupSearchRef.resetTime();
		pickupSearchRef.setInitialRef(currentRefGen.getRef());
		currentRefGen = pickupSearchRef;
	}
	
	/** called from SwitchThread */
	public synchronized void setRefGenTrajectorySmall(){
		throwRefSmall.resetTime();
		currentRefGen = throwRefSmall;
	}
	
	/** called from SwitchThread */
	public synchronized void setRefGenTrajectoryMedium(){
		throwRefMedium.resetTime();
		currentRefGen = throwRefMedium;
	}
	
	/** called from SwitchThread */
	public synchronized void setRefGenTrajectoryLarge(){
		throwRefLarge.resetTime();
		currentRefGen = throwRefLarge;
	}
	
	public synchronized double getRef() {
		return currentRefGen.getRef();
	}
	
	/** called by RegulThread*/
	public synchronized double calcOutput(double[] y, double yref) {
		if(currentRegul == null) {
			return 0.0;
		} else {
			return currentRegul.calculateOutput(y, yref, h);
		}
	}
	
	/** called by RegulThread*/
	public synchronized void updateState() {
		if(currentRegul != null) {
			currentRegul.updateState(h);
		}
	}
	
	/** called by Opcom*/
	public synchronized void setInnerParameters(PIDParameters p) {
		beamRegul.setParameters(p);
	}
	
	/** called by Opcom*/
	public synchronized PIDParameters getInnerParameters() {
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
		beamRegul.reset();
		currentRegul = beamRegul; //update currentRegul
	}
	
	/** called by Opcom*/
	public synchronized void setBallMode(){
		mode = BALL;
		beamBallRegul.reset();
		currentRegul = beamBallRegul; //update currentRegul
	}
	
	/** called by Opcom*/
	public synchronized void setSequenceMode(){
		mode = SEQUENCE;
//		beamBallRegul.reset();
//		currentRegul = beamBallRegul; //update currentRegul
		
		/* Write semaphore thing for "starting SwitchThread" */
	}
	
	/** called by SwitchThread and Opcom*/
	public synchronized int getMode() {
		return mode;
	}
	
	/** called by OpCom*/
	public synchronized void setH(double h) {
		this.h = h;
	}
	
	/** called by OpCom*/
	public synchronized double getH() {
		return h;
	}
	
	/** called by RegulThread*/
	public synchronized long getHMillis() {
		return (long) (h*1000.0);
	}
	
	
	
}	