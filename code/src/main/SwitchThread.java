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
	private double gravityForceSmall = 1.29, gravityForceMedium = 3.11, gravityForceLarge = 7.6;//?,3.42,8.31; // These are not in SI units!
	private int weight = -1;

	/**
	 * Some choices of positions, times etc.
	 */
	private final double pickupStartAngle = -0.04; // From which angle (radians) the search for ball magazine will start.
	private final double pickupEndAngleBias = 0.01; // Lower beam a little so that the ball actually slides on.
	private final double pickupRampSlope = -0.015; // Angular velocity of beam when searching for ball magazine
	private final double ballCatchPosition = 0.2; // Used to stop ball on pickup, not waiting for this to be achieved
	private final double ballWeighPosition = 0.45;
	private final double ballThrowPosition = -0.15;

	/** Constructor */
	public SwitchThread(Monitor monitor, Semaphore sem, int prio) {
		mon = monitor;
		setPriority(prio);
		mon.setRefGenConstantAngle(-0.1); //TODO experiment with this value
		try {
			digitalOut = new DigitalOut(0);
			digitalOut.set(true); // Do not drop ball
		} catch (IOChannelException e) {
			e.printStackTrace();
		}




	}

	public void run() {

		while (shouldRun) {
			System.out.println("Sequence mode ready to go.");
			//The whole loop has to be synchronized, in case someone chooses sequence mode
			//between loop evaluation and call to wait().
			synchronized(mon) {
				// Wait until sequence mode
				while (mon.getMode() != Monitor.SEQUENCE) {
					try {
						mon.wait();
						//							Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				mon.clearResetSequence(); //clear reset flag to enable function calls
			}
			System.out.println("Starting sequence!");
			synchronized (mon) {
				// set the reference value of the beam angle to 0
				mon.setBeamRegul();
				mon.setRefGenConstantAngle(pickupStartAngle);
			}

			System.out.println("Waiting for constant initial pickup angle");

			// wait until the beam angle has become 0, this method calls wait()
			mon.setConstBeamCheck(pickupStartAngle);
			System.out.println("At constant pickup");

			// Move beam towards catch position
			mon.setRefGenRampAngle(pickupRampSlope);
			System.out.println("Waiting for LED");
			// wait until the beam is at the catch position, this method calls wait()
			mon.setLEDCheck();
			System.out.println("LED noticed");
			// Move beam a bit down
			mon.setRefGenConstantAngle(mon.getRef()[ReferenceGenerator.ANGLE] + pickupEndAngleBias); // keep
			System.out.println("Waiting for constant actual pickup angle");
			mon.setConstBeamCheck(mon.getRef()[ReferenceGenerator.ANGLE]);
			System.out.println("Reached constant angle, shooting!");


			fire(true); // Reset the solenoid to let ball take position in front of solenoid
			try {
				// Hooooold...
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// FIRE!
			fire(false); // Push ball on beam

			System.out.println("FIRE! Sleeping until ball on beam (short time)");

			try {//Wait for ball on beam!!
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			fire(true); // Reset the solenoid again

			// switch to ball control at safe (left position)
			synchronized (mon) {
				mon.setBallRegul();
				mon.setRefGenConstantPos(ballCatchPosition);
				mon.setNullCheck();
			}
//			// Wait for stability0
//			try {
//				Thread.sleep(5);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			// Wait for stability
			System.out.println("Go to weigh position");
			double rampSlope1 = (ballCatchPosition-ballWeighPosition)/((double)6000/1000);
			mon.setRefGenRampToPos(rampSlope1,ballWeighPosition);
			mon.setConstBallCheck(ballWeighPosition);


			mon.setNullCheck();

			double averageControlSignal = mon.getAverageControlSignal();
			double currentBallPos = mon.getBallPosition();
			weight = checkWeight(averageControlSignal / currentBallPos);
			System.out.println("WEIGHT: "+weight+" Value: "+averageControlSignal / currentBallPos);
			switch(weight) {
			case SMALL:
				long smallFirstRampTime = 350;//ms
				long smallSecondRampTime = 200;//ms
				long smallWaitTime = 250;
				double smallFirstAngle = 0.55;
				double smallSecondAngle = -0.18;
				synchronized(mon) {
					mon.setBeamRegul();
					double rampSlope = smallFirstAngle/((double)smallFirstRampTime/1000);
					mon.setRefGenRampToAngle(rampSlope,smallFirstAngle); //TODO experiment with this value
				}
				try {
					Thread.sleep(smallFirstRampTime+smallWaitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized(mon) {
					mon.setBeamRegul();
					double rampSlope = (smallSecondAngle-smallFirstAngle)/((double)smallSecondRampTime/1000);
					mon.setRefGenRampToAngle(rampSlope,smallSecondAngle); //TODO experiment with this value
				}
				break;
			case MEDIUM:
				double firstAngleRampTime = 110;//ms
				double secondAngleRampTime = 100;//ms
				long throwSleepTime =  500;
				double throwFirstAngle = -0.60;
				double throwSecondAngle = 0.2;
				if(heuristicApproach) {
					long rampMoveTime = 6000;//ms
					synchronized(mon) {
						double rampSpeed = (ballThrowPosition-ballWeighPosition)/((double) rampMoveTime/1000);
						mon.setRefGenRampToPos(rampSpeed,ballThrowPosition);
					}
					try {
						Thread.sleep(rampMoveTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized(mon) {
						double rampSpeed = 0.03;
						mon.setRefGenRampToPos(rampSpeed,ballThrowPosition-0.25);
						mon.setConstBallCheck(ballThrowPosition-0.25);
					}
					synchronized(mon) {

						double rampSpeed = throwFirstAngle/((double) firstAngleRampTime/1000);
						mon.setBeamRegul();
						mon.setRefGenRampToAngle(rampSpeed,throwFirstAngle);
					}
					try {
						Thread.sleep((long) (firstAngleRampTime+throwSleepTime));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized(mon) {
						double rampSpeed = (throwSecondAngle-throwFirstAngle)/((double) secondAngleRampTime/1000);
						mon.setBeamRegul();
						mon.setRefGenRampToAngle(rampSpeed,throwSecondAngle);
					}
				} else {
					// Do something with TrajectoryRef
				}
				break;
			case LARGE:
				synchronized(mon) {
					mon.setBeamRegul();
					mon.setRefGenConstantAngle(-0.03); //TODO experiment with this value
				}

				break;
			}
			try {//Wait for ball to be done
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

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

		if(value < (gravityForceSmall+gravityForceMedium)/2) {
			return SMALL;
		} else if(value < (gravityForceMedium+gravityForceLarge)/2) {
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