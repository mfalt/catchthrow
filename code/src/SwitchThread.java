import java.io.IOException;

import se.lth.control.realtime.DigitalIn;
import se.lth.control.realtime.DigitalOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;


public class SwitchThread extends Thread {
	private Monitor mon;
	private boolean shouldRun = true; 
	private Semaphore sem;
	private double epsilon = 0.01; //close to 0

	private DigitalIn digitalIn; 			// sensor light
	private DigitalOut digitalOut;
	
	public final int SMALL = 0, MEDIUM = 1, LARGE = 2;

	/** Constructor */
	public SwitchThread(Monitor monitor, Semaphore sem, int prio) {
		mon = monitor;
		setPriority(prio);
		this.sem = sem;
		try {
			digitalIn = new DigitalIn(0);
			digitalOut = new DigitalOut(0);
			digitalOut.set(true); // Do not drop ball
		} catch (IOChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public void run() {

		//		TEST CODE FOR FOLLOWING THROW TRAJECTORY (comment out the while loop if you want to test)
		//		synchronized(mon) {
		//			mon.setBeamRegul();
		//			mon.setRefGenTrajectoryMedium();
		//		}


		//TODO: Currently (mostly) assumes we will never change away from sequence mode after activating
		while(shouldRun){
			try{
				//Wait until woken in sequence mode
				synchronized(mon){
					while(mon.getMode()!=Monitor.SEQUENCE){
						mon.wait();
					}
				}

				//			sem.take();
				//			if(!shouldRun){
				//				break;
				//			}

				synchronized(mon){
					//Make sure angle is 0 before continuing
					mon.setBeamRegul();
					mon.setRefGenConstantAngle(0.0);
					mon.setConstBeamCheck(0.0);
					mon.wait(); //wakes up when angle is 0 (in theory!)
					//if wait doesn't work, just sleep() (outside the monitor!!) for testing
					
				//This makes the thread sleep outside the monitor to allow other threads to access the monitor
				//Uncomment if the checker doesn't work and you need to test stuff
//				}
//				Thread.sleep(300);
//				synchronized(mon){
					
					//Move beam towards catch position
					mon.setRefGenRampAngle(-1.0, ReferenceGenerator.ANGLE);
					mon.setLEDCheck();
					mon.wait(); //wakes up when beam is at catch position
					
					//Make sure  beam is stationary before continuing
					mon.setRefGenConstantAngle(mon.getRef()[ReferenceGenerator.ANGLE]); //keep beam at angle
					mon.setConstBeamCheck(mon.getRef()[ReferenceGenerator.ANGLE]);
					mon.wait();
				}
				
				//Ready...
				FIRE(false);
				try {
					//Hooooold...
					Thread.sleep(400); //measure proper time
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//FIRE!
				FIRE(true); //TODO: loop if no ball is released
				
				Thread.sleep(200); //TODO: find good time or change to detecting if ball is on beam
				
				synchronized(mon){
					mon.setBallRegul();
					mon.setRefGenConstantPos(3.0);
					mon.setConstBallCheck(3.0);
					mon.wait();
				}
				
				//Make ball weight decision
				mon.setNullCheck();
				int ballSize = -1;
				
				if(ballSize == SMALL){
					
				} else if(ballSize == MEDIUM) {
					
				} else if(ballSize == LARGE) {
					
				}
				
				mon.setOFFMode(); //won't update GUI?
			} catch(InterruptedException e){
				Thread.interrupted();
				continue;
			}
		}

	}

	private boolean getLED(){
		try {
			return digitalIn.get();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void FIRE(boolean push){
		try {
			digitalOut.set(push);
		} catch (IOChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void shutdown() {
		shouldRun = false;
	}
}
