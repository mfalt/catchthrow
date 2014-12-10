package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import refgen.*;
import regul.*;
import checker.*;


public class Monitor {

	/** Controllers */
	private BeamRegul beamRegul;
	private BeamBallRegul beamBallRegul;
	//	private BeamBallRegul beamBallThrowSmall;
	//	private BeamBallRegul beamBallThrowMedium;
	//	private BeamBallRegul beamBallThrowLarge;
	private Regul currentRegul;

	/** Reference generators */
	private RefGenGUI refGenGUIPos;
	private RefGenGUI refGenGUIAngle;
	private ConstantRef constantPosRef;
	private ConstantRef constantAngleRef;
	private ConstantVectorRef constantVectorRef;
	private ConstPosRampAngleRef constPosRampAngleRef;
	private RampRef rampPosRef;
	private RampRef rampAngleRef;
	private RampToRef rampToPosRef;
	private RampToRef rampToAngleRef;
	private TrajectoryRef throwRefSmall;
	private TrajectoryRef throwRefMedium;
	private TrajectoryRef throwRefLarge;
	private ReferenceGenerator currentRefGen;

	private StateChecker stateCheck;
	private LEDChecker ledCheck;
	private ConstBallChecker constBallCheck;
	private ConstBeamChecker constBeamCheck;
	private BallOnBeamChecker ballOnBeamCheck;

	public static final int OFF=0, BEAM=1, BALL=2, SEQUENCE=3;
	private int mode;
	private double h = 0.02, currentControlSignal = 0;
	private double y[] = {0.0,0.0}; //beam angle, ball position
	private double averageControlSignal = 0;

	private boolean resetSequence = false;

	/** Constructor*/
	public Monitor() {
		mode = OFF; //mode is the state of our program
		beamRegul = new BeamRegul();
		beamBallRegul = new BeamBallRegul(beamRegul);

		constantPosRef = new ConstantRef(ReferenceGenerator.POS);
		constantAngleRef = new ConstantRef(ReferenceGenerator.ANGLE);
		constantVectorRef = new ConstantVectorRef();
		constPosRampAngleRef = new ConstPosRampAngleRef();
		rampPosRef = new RampRef(ReferenceGenerator.POS);
		rampAngleRef = new RampRef(ReferenceGenerator.ANGLE);
		rampToPosRef = new RampToRef(ReferenceGenerator.POS);
		rampToAngleRef = new RampToRef(ReferenceGenerator.ANGLE);

		constBeamCheck = new ConstBeamChecker();
		constBallCheck = new ConstBallChecker();
		ballOnBeamCheck = new BallOnBeamChecker();
		ledCheck = new LEDChecker();

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

	/** Called from RegulThread */
	public synchronized double[] getRef() {
		return currentRefGen.getRef();
	}

	/** called from Main */
	public synchronized void initRefGenGUI(RefGenGUI refGenGUIPos, RefGenGUI refGenGUIAngle){
		this.refGenGUIPos = refGenGUIPos;
		this.refGenGUIAngle = refGenGUIAngle;
		currentRefGen = refGenGUIPos;
	}

	/** called from SwitchThread */
	public synchronized void setRefGenConstantPos(double r){
		if(!resetSequence){
			constantPosRef.setRef(r);
			currentRefGen = constantPosRef;
		}
	}

	/** called from SwitchThread */
	public synchronized void setRefGenConstantAngle(double r){
		if(!resetSequence){
			constantAngleRef.setRef(r);
			currentRefGen = constantAngleRef;
		}
	}

	/** called from SwitchThread */
	public synchronized void setRefGenConstantPosAndAngle(double posRef, double angleRef){
		if(!resetSequence){
			constantVectorRef.setZeroRef();
			constantVectorRef.setPosRef(posRef);
			constantVectorRef.setAngleRef(angleRef);
			currentRefGen = constantVectorRef;
		}
	}

	/** called from SwitchThread */
	public synchronized void setRefGenConstPosRampAngle(double posRef, double angleRampSlope){
		if(!resetSequence){
			constPosRampAngleRef.setRef(posRef, angleRampSlope);
			constPosRampAngleRef.setInitialAngleRef(currentRefGen.getRef()[ReferenceGenerator.ANGLE]);
			currentRefGen = constPosRampAngleRef;
		}
	}

	/** called from SwitchThread */
	public synchronized void setRefGenRampPos(double rampSlope){
		if(!resetSequence){
			rampPosRef.setRef(rampSlope);
			//rampAngleRef.resetTime();
			rampPosRef.setInitialRef(currentRefGen.getRef()[ReferenceGenerator.POS]);
			currentRefGen = rampPosRef;
		}
	}

	/** called from SwitchThread */
	public synchronized void setRefGenRampAngle(double rampSlope){
		if(!resetSequence){
			rampAngleRef.setRef(rampSlope);
			//rampAngleRef.resetTime();
			rampAngleRef.setInitialRef(currentRefGen.getRef()[ReferenceGenerator.ANGLE]);
			currentRefGen = rampAngleRef;
		}
	}

	/** called from SwitchThread */
	public synchronized void setRefGenRampToPos(double speed, double finalPos){
		if(!resetSequence){
			rampToPosRef.setRef(currentRefGen.getRef()[ReferenceGenerator.POS], speed, finalPos);
//			rampToPosRef.setInitialRef(currentRefGen.getRef()[ReferenceGenerator.POS]);
			currentRefGen = rampToPosRef;
		}
	}

	/** called from SwitchThread */
	public synchronized void setRefGenRampToAngle(double speed, double finalAngle){
		if(!resetSequence){
			rampToAngleRef.setRef(currentRefGen.getRef()[ReferenceGenerator.ANGLE], speed, finalAngle);
//			rampToAngleRef.setInitialRef(currentRefGen.getRef()[ReferenceGenerator.ANGLE]);
			currentRefGen = rampToAngleRef;
		}
	}

	/** called from SwitchThread */
	/*public synchronized void setRefGenTrajectorySmall(){
		if(!resetSequence){
			throwRefSmall.resetTime();
			currentRefGen = throwRefSmall;
		}
	}*/

	/** called from SwitchThread */
	/*public synchronized void setRefGenTrajectoryMedium(){
		if(!resetSequence){
			throwRefMedium.resetTime();
			currentRefGen = throwRefMedium;
		}
	}*/

	/** called from SwitchThread */
	/*public synchronized void setRefGenTrajectoryLarge(){
		if(!resetSequence){
			throwRefLarge.resetTime();
			currentRefGen = throwRefLarge;
		}
	}*/

	/** called by RegulThread*/
	public synchronized double calcOutput(double[] measurement) {
		this.y[0] = measurement[0];
		this.y[1] = measurement[1];
		//		LED = digitalValue;
		if(currentRegul == null) {
			return 0.0;
		} else {
			currentControlSignal = currentRegul.calculateOutput(measurement, currentRefGen.getRef(), h);
			averageControlSignal = averageControlSignal*0.97+currentControlSignal*0.03;
			return currentControlSignal;
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
		resetSequence = true;
		notifyAll();
	}

	/** called by Opcom*/
	public synchronized void setBeamMode(){
		mode = BEAM;
		beamRegul.reset(y);
		currentRegul = beamRegul; //update currentRegul
		currentRefGen = refGenGUIAngle; //ifall man var i sequence mode innan?
		resetSequence = true;
		notifyAll();
	}

	/** called by Opcom*/
	public synchronized void setBallMode(){
		mode = BALL;
		beamBallRegul.reset(y);
		currentRegul = beamBallRegul; //update currentRegul
		currentRefGen = refGenGUIPos;
		resetSequence = true;
		notifyAll();
	}

	/** called by Opcom*/
	public synchronized void setSequenceMode(){
		mode = SEQUENCE; 
		//resetSequence NOT set on purpose
		notifyAll();
	}

	/** called by SwitchThread*/
	public synchronized void setBeamRegul(){
		if(!resetSequence){
			beamRegul.reset(y);
			currentRegul = beamRegul;
		}
	}

	/** called by SwitchThread*/
	public synchronized void setBallRegul(){
		if(!resetSequence){
			beamBallRegul.reset(y);
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

	public synchronized double getBeamAngle(){
		return y[0];
	}

	public synchronized double getBallPosition(){
		return y[1];
	}

	public synchronized double getCurrentControlSignal(){
		return currentControlSignal;
	}

	public synchronized double getAverageControlSignal(){
		return averageControlSignal;
	}

	public synchronized void checkState() {
		if (stateCheck != null && stateCheck.check(y)) {
			notifyAll();
		}
	}

	//Called by SwitchThread
	public synchronized void setConstBeamCheck(double y) {
		if(!resetSequence){
			stateCheck = constBeamCheck;
			constBeamCheck.reset();
			constBeamCheck.setValue(y);
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//Called by SwitchThread
	public synchronized void setConstBallCheck(double y) {
		if(!resetSequence){
			stateCheck = constBallCheck;
			constBallCheck.reset();
			constBallCheck.setValue(y);
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//Called by SwitchThread
	public synchronized void setBallOnBeamCheck() {
		if(!resetSequence){
			stateCheck = ballOnBeamCheck;
			ballOnBeamCheck.reset();
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//Called by SwitchThread?
	public synchronized void setNullCheck() {
		stateCheck = null;
	}

	//Called by SwitchThread
	public synchronized void setLEDCheck() {
		if(!resetSequence){
			stateCheck = ledCheck;
			ledCheck.reset();
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//Called by SwitchThread
	public synchronized void clearResetSequence(){
		resetSequence = false;
	}

}
