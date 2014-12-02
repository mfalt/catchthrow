import java.io.FileNotFoundException;
import java.io.IOException;


public class Monitor {

	private BeamRegul beamRegul;
	private BeamBallRegul beamBallRegul;
	//	private BeamBallRegul beamBallThrowSmall;
	//	private BeamBallRegul beamBallThrowMedium;
	//	private BeamBallRegul beamBallThrowLarge;
	private Regul currentRegul;

	private RefGenGUI refGenGUI;
	private ConstantRef constantAngleRef;
	private RampRef rampAngleRef;
	private ConstantRef constantPosRef;
	private TrajectoryRef throwRefSmall;
	private TrajectoryRef throwRefMedium;
	private TrajectoryRef throwRefLarge;
	private ReferenceGenerator currentRefGen;
	
	private StateChecker stateCheck;
	private LEDChecker ledCheck;
	private ConstBallChecker constBallCheck;
	private ConstBeamChecker constBeamCheck;

	public static final int OFF=0, BEAM=1, BALL=2, SEQUENCE=3;
	private int mode;
	private double h = 0.02;

	private double y[] = {0.0,0.0}; //beam angle, ball position
	//	private boolean LED = false;

	/** Constructor*/
	public Monitor() {
		mode = OFF; //mode is the state of our program
		beamRegul = new BeamRegul();
		beamBallRegul = new BeamBallRegul(beamRegul);

		
		constantAngleRef = new ConstantRef();
		constantAngleRef.pickState(ReferenceGenerator.ANGLE);
		rampAngleRef = new RampRef();
		rampAngleRef.pickState(ReferenceGenerator.ANGLE);
		constantPosRef = new ConstantRef();
		constantPosRef.pickState(ReferenceGenerator.POS);
		
		try {
			throwRefSmall = new TrajectoryRef("../simulink_test/throwPath.mat");
			throwRefMedium = new TrajectoryRef("../simulink_test/throwPath.mat");
			throwRefLarge = new TrajectoryRef("../simulink_test/throwPath.mat");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** called from Main */
	public synchronized void setRefGenGUI(RefGenGUI referenceGenerator){
		refGenGUI = referenceGenerator;
		currentRefGen = refGenGUI;
	}

	/** called from SwitchThread */
	public synchronized void setRefGenConstantPos(double r){
		constantPosRef.setRef(r);
		currentRefGen = constantPosRef;
	}

	/** called from SwitchThread */
	public synchronized void setRefGenConstantAngle(double r){
		constantAngleRef.setRef(r);
		currentRefGen = constantAngleRef;
	}

	/** called from SwitchThread */
	public synchronized void setRefGenRampAngle(double rampSlope, int state){
		rampAngleRef.setRampSlope(rampSlope);
		rampAngleRef.resetTime();
		rampAngleRef.setInitialRef(currentRefGen.getRef()[state]);
		currentRefGen = rampAngleRef;

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

	public synchronized double[] getRef() {
		return currentRefGen.getRef();
	}

	/** called by RegulThread*/
	public synchronized double calcOutput(double[] measurement) {
		this.y[0] = measurement[0];
		this.y[1] = measurement[1];
		//		LED = digitalValue;
		if(currentRegul == null) {
			return 0.0;
		} else {
			return currentRegul.calculateOutput(measurement, currentRefGen.getRef(), h);
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
		notifyAll();
		//		beamBallRegul.reset();
		//		currentRegul = beamBallRegul; //update currentRegul

		/* Write semaphore thing for "starting SwitchThread" */
	}

	/** called by SwitchThread*/
	public synchronized void setBeamRegul(){
		if(mode == SEQUENCE){
			beamRegul.reset();
			currentRegul = beamRegul;
		}
	}

	/** called by SwitchThread*/
	public synchronized void setBallRegul(){
		if(mode == SEQUENCE){
			beamBallRegul.reset();
			currentRegul = beamBallRegul;
		}
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

	//	public synchronized boolean getLED(){
	//		return LED;
	//	}

	public synchronized double getBeamAngle(){
		return y[0];
	}

	public synchronized double getBallPosition(){
		return y[1];
	}


	public synchronized boolean checkState(){
		if(stateCheck==null){
			return false; //returnera true eller false här?
		}
		return stateCheck.check(y);
	}
	
	public synchronized void setConstBeamCheck(double y){
		stateCheck = constBeamCheck;
		constBeamCheck.setValue(y);
	}
	
	public synchronized void setConstBallCheck(double y){
		stateCheck = constBallCheck;
		constBallCheck.setValue(y);
	}
	
	public synchronized void setNullCheck(){
		stateCheck = null;
	}
	
	public synchronized void setLEDCheck() {
		stateCheck = ledCheck;
	}
}	