import java.io.IOException;

import se.lth.control.realtime.DigitalOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;

public class SwitchThread extends Thread {
	private Monitor mon;
	private boolean shouldRun = true;
	private DigitalOut digitalOut;

	public final int SMALL = 0, MEDIUM = 1, LARGE = 2;
	private double currentControlSignal = 0;
	private double uSmall = 0, uMedium = 0, uLarge = 0;
	private int weight = 0;

	
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
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
					
				System.out.println("#1");
		
				synchronized (mon) {
					// set the reference value of the beam angle to 0
					mon.setBeamRegul();
					mon.setRefGenConstantAngle(-0.05);
				}
				
				System.out.println("#2");
				// wait until the beam angle has become 0, this method calls wait()
				mon.setConstBeamCheck(-0.05);
				System.out.println("#3");
				
				// Move beam towards catch position
				mon.setRefGenRamp(-0.1, ReferenceGenerator.ANGLE);
				// wait until the beam is at the catch position, this method calls wait()
				System.out.println("#4");
				mon.setLEDCheck();

				System.out.println("#5");
				// Make sure beam is stationary before continuing
				mon.setRefGenConstantAngle(mon.getRef()[ReferenceGenerator.ANGLE]); // keep
																						// beam
				System.out.println("#6");
																						// at angle
				mon.setConstBeamCheck(mon.getRef()[ReferenceGenerator.ANGLE]);
				// Ready...
				fire(false);
				// here we should sleep until the ball is detected
				while(mon.getBallPosition() <= -0.45) {
					try {
						// Hooooold...
						Thread.sleep(10); // measure proper time for the ball to fall on the beam
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// FIRE!
				fire(true);
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				} //TODO: find good time or change to detecting if ball is on beam
				
				// switch to ball control and wait until the ball is at position 3.0 for example
				synchronized (mon) {
					mon.setBallRegul();
					mon.setRefGenConstantPos(0.35);
					mon.setConstBallCheck(0.35);
				}
				// Make ball weight decision
				mon.setNullCheck();
				currentControlSignal = mon.getCurrentControlSignal();
				weight = checkWeight(currentControlSignal);
			
//				switch(weight) {
//				case SMALL:
//
//				case MEDIUM:
//					synchronized(mon) {
//						
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
		if (currentControlSignal >= uSmall && currentControlSignal < uMedium) {
			return SMALL;
		} else if (currentControlSignal >= uMedium && currentControlSignal < uLarge) {
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