package main;

import java.io.IOException;

import se.lth.control.realtime.DigitalOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;
import refgen.*;

public class SwitchThread extends Thread {
	private Monitor mon;
	private boolean shouldRun = true;
	private DigitalOut digitalOut;

	private final boolean heuristicApproach = true; // Use this boolean instead of heuristic branch
	
	public final int SMALL = 0, MEDIUM = 1, LARGE = 2;
	private double gravityForceSmall = 0, gravityForceMedium = 0, gravityForceLarge = 0; // These are not in SI units!
	private int weight = -1;
	
	/**
	 * Some choices of positions, times etc.
	 */
	private final double pickupStartAngle = -0.09; // From which angle (radians) the search for ball magazine will start.
	private final double pickupEndAngleBias = 0.02; // Lower beam a little so that the ball actually slides on.
	private final double pickupRampSlope = -0.015; // Angular velocity of beam when searching for ball magazine
	private final double ballWeighPosition = 0.35; // 35 cm

	
	/** Constructor */
	public SwitchThread(Monitor monitor, Semaphore sem, int prio) {
		mon = monitor;
		setPriority(prio);
		try {
			digitalOut = new DigitalOut(0);
			digitalOut.set(true); // Do not drop ball
		} catch (IOChannelException e) {
			e.printStackTrace();
		}



		
	}

	public void run() {

		while (shouldRun) {
			//try{
		
				// Wait until sequence mode
					while (mon.getMode() != Monitor.SEQUENCE) {
						try {
							synchronized(mon) {
								mon.wait();
							}
//							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
		
				synchronized (mon) {
					// set the reference value of the beam angle to 0
					mon.setBeamRegul();
					mon.setRefGenConstantAngle(pickupStartAngle);
				}
				
				// wait until the beam angle has become 0, this method calls wait()
				mon.setConstBeamCheck(pickupStartAngle);
				
				// Move beam towards catch position
				mon.setRefGenRampAngle(pickupRampSlope);
				// wait until the beam is at the catch position, this method calls wait()
				mon.setLEDCheck();

				// Make sure beam is stationary before continuing
				mon.setRefGenConstantAngle(mon.getRef()[ReferenceGenerator.ANGLE] + pickupEndAngleBias); // keep
																						// beam
																						// at angle
				mon.setConstBeamCheck(mon.getRef()[ReferenceGenerator.ANGLE]);
				
				fire(true); // Reset the solenoid to let ball take position in front of solenoid
				try {
					// Hooooold...
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// FIRE!
				fire(false); // Push ball on beam
				mon.setBallOnBeamCheck();
				fire(true); // Reset the solenoid again
				
				// switch to ball control and wait until the ball is at weighing position
				synchronized (mon) {
					mon.setBallRegul();
					mon.setRefGenConstantPos(ballWeighPosition);
//					mon.setRefGenConstantPosAndAngle(ballWeighPosition, -mon.getRef()[ReferenceGenerator.ANGLE]); // FF to retrieve ball better, not entirely sure of this
					mon.setConstBallCheck(ballWeighPosition);
				}
				// Make ball weight decision
				mon.setNullCheck();
				double currentControlSignal = mon.getCurrentControlSignal(); // Take MANY measurements and average since the signal is VERY noisy!
				double currentBallPos = mon.getBallPosition();
				weight = checkWeight(currentControlSignal / currentBallPos);
			
//				switch(weight) {
//				case SMALL:
//
//				case MEDIUM:
//					if(heuristicApproach) {
//						synchronized(mon) {
//							
//						}
//					} else {
//						// Do something with TrajectoryRef
//					}
//					break;
//				case LARGE:
//					synchronized(mon) {
//						mon.setBeamRegul();
//						mon.setRefGenConstantAngle(-5.0); //TODO experiment with this value
//					}
//					break;
//				}
				
			//} catch(InterruptedException e){
			//Thread.interrupted();
			//continue;
			//}
		}
	}
	
	private int checkWeight(double value) {
		/**
		 * Due to stiction we will have uncertainties in the position of the ball when weighing.
		 * Make decision of ball weight based on currentControlSignal/currentBallPos instead of just currentControlSignal.
		 * 
		 * Gravity force variables are not in SI units, so they have to be measured looking at the control signal and
		 * dividing with the ball position for each ball.
		 */
		if (value >= gravityForceSmall && value < gravityForceMedium) {
			return SMALL;
		} else if (value >= gravityForceMedium && value < gravityForceLarge) {
			return MEDIUM;
		} else {
			return LARGE;
		}
	}

	private void fire(boolean push) {
		try {
			digitalOut.set(push);
		} catch (IOChannelException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		shouldRun = false;
	}
}