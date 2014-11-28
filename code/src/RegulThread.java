import java.io.IOException;

import se.lth.control.DoublePoint;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.DigitalIn;
import se.lth.control.realtime.DigitalOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;


public class RegulThread extends Thread {
	
	private Monitor mon;
	private OpCom opcom;
	private boolean shouldRun = true;
	private Semaphore mutex; // used for synchronization at shut-down
	private long startTime;
	
	//input and output analog signals to and from the real process
	private AnalogIn analogInAngle;        // angle of the beam = yAngle
	private AnalogIn analogInPosition;     // position of the ball = yPos
	private AnalogOut analogOut;           // torque for the beam = uAngle
	
//	private DigitalIn digitalIn; 			// sensor light
//	private DigitalOut digitalOut;
	
	private double uAngle, ref;
	private double[] analogValues;  //yAngle on index 0, yPos on index 1
//	private boolean digitalValue;
	
	/** Constructor */
	public RegulThread(Monitor monitor, int prio) {
		
		//set up the analog signals
		try {
			analogInAngle = new AnalogIn(0);
			analogInPosition = new AnalogIn(1);
			analogOut = new AnalogOut(0);
//			digitalIn = new DigitalIn(0);
//			digitalOut = new DigitalOut(0);
//			digitalOut.set(true); // Do not drop ball
		} catch (IOChannelException e) { 
			System.out.print("Error: IOChannelException: ");
			System.out.println(e.getMessage());
		}
		
		analogValues = new double[2]; 
		mon = monitor;
		mutex = new Semaphore(1);
		setPriority(prio);
	}
	
	/** called from Main */
	public void setOpCom(OpCom opcom) {
		this.opcom = opcom;
	}
	
	private void sendDataToOpCom(double yref, double[] y, double u) {
		double x = (double)(System.currentTimeMillis() - startTime) / 1000.0;
		DoublePoint dp = new DoublePoint(x,u);
		int mode = mon.getMode();
		PlotData pd = new PlotData(x,(mode == Monitor.BEAM) ? yref : -10,y[0]);
		PlotData pd2 = new PlotData(x,(mode == Monitor.BALL) ? yref : -10,y[1]);
		opcom.putControlDataPoint(dp);
		opcom.putMeasurementDataPoint(pd);
		opcom.putMeasurement2DataPoint(pd2);
	}
	
	/** Called from OpCom when shutting down */
	public synchronized void shutDown() {
		shouldRun = false;
		mutex.take();
		try {
			analogOut.set(0.0);
		} catch (IOChannelException x) {
		}
	}
	
	public void run() {
		startTime = System.currentTimeMillis();
		long t = startTime;
		mutex.take();
		while(shouldRun) {
			
//			try {
//				digitalValue = digitalIn.get();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			//get the angle of the beam and its set point
			try {
				analogValues[0] = analogInAngle.get();
			} catch (IOChannelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//get the position of the ball
			try {
				analogValues[1] = analogInPosition.get();
			} catch (IOChannelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
						
			synchronized(mon){ //to get synchronization between calcOutput and updateState
				uAngle = mon.calcOutput(analogValues);
			
				//send the control signal to the real process
				try {
					analogOut.set(uAngle);
				} catch (IOChannelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(mon.checkState()){
					mon.notifyAll(); //wake up switchthread (might change how this is done)
				}
				
				//might have to rethink this part...
				ref = mon.getRef();

				sendDataToOpCom(ref, analogValues, uAngle);
				//System.out.println(ref);
				
				//if we do updateState here then this above calculation
				//is done while holding the monitor but if we put the calculations outside
				//the synchronized block then we get a bigger delay in the 
				//realtime plot...I think it may be better here because
				//opcom does not need the monitor to update the plot anyway
				mon.updateState();
			}
			t = t + mon.getHMillis();
			long duration = t-System.currentTimeMillis();
			//System.out.println(duration);
			if(duration > 0) {
				try {
					sleep(duration);
				} catch(InterruptedException e) {
					
				}
			}
		}
		mutex.give();
		
	}

}
