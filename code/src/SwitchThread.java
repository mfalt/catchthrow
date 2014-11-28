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
		while(shouldRun){
//			sem.take();
//			if(!shouldRun){
//				break;
//			}

			mon.setBeamRegul();
			mon.setRefGenConstantAngle(0.0);

			//TODO:make sure beam is stationary at 0


			mon.setRefGenRamp(-1.0, ReferenceGenerator.ANGLE);
			while(!getLED()&& mon.getMode()==Monitor.SEQUENCE){

				synchronized (mon) {
					try {
						mon.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			synchronized(mon){
				mon.setRefGenConstantAngle(mon.getRef()[ReferenceGenerator.ANGLE]); //keep beam at angle
			}

			FIRE(false);
			try {
				Thread.sleep(400); //measure proper time
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FIRE(true);







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
