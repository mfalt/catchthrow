import se.lth.control.DoublePoint;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;


public class RegulThread extends Thread {
	
	private Monitor mon;
	private OpCom opcom;
	private boolean shouldRun = true;
	private Semaphore mutex; // used for synchronization at shut-down
	
	//input and output analog signals to and from the real process
	private AnalogIn analogInAngle;        // angle of the beam = yAngle
	private AnalogIn analogInPosition;     // position of the ball = yPos
	private AnalogOut analogOut;           // angle of the beam = uAngle
	
	private double uAngle, ref;
	private double[] analogValues;  //yAngle on index 0, yPos on index 1
	private ReferenceGenerator referenceGenerator;
	
	/** Constructor */
	public RegulThread(Monitor monitor, int prio) {
		
		//set up the analog signals
		try {
			analogInAngle = new AnalogIn(0);
			analogInPosition = new AnalogIn(1);
			analogOut = new AnalogOut(0);
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
	public void setRefGen(ReferenceGenerator referenceGenerator){
		this.referenceGenerator = referenceGenerator;
	}
	
	/** called from Main */
	public void setOpCom(OpCom opcom) {
		this.opcom = opcom;
	}
	
	/** anti wind-up */
	private double limit(double v, double min, double max) {
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return v;
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
		long startTime = System.currentTimeMillis();
		
		mutex.take();
		while(shouldRun) {
			
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
			
			ref = referenceGenerator.getRef();
			
			synchronized(mon){ //to get synchronization between calcOutput and updateState
				uAngle = mon.calcOutput(analogValues, ref);
				uAngle = limit(uAngle, -10, 10); //anti-windup
			
				//send the control signal to the real process
				try {
					analogOut.set(uAngle);
				} catch (IOChannelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
				//hmm... plots the beam angle and its reference
				//to be changed if we want to plot the ball position and its ref
				//must rethink this part...
				double x = (double)(System.currentTimeMillis() - startTime) / 1000.0;
				DoublePoint dp = new DoublePoint(x,uAngle);
				PlotData pd = new PlotData(x,ref,analogValues[0]);
				opcom.putControlDataPoint(dp);
				opcom.putMeasurementDataPoint(pd);
				
				//if we do updateState here then this above calculation
				//is done while holding the monitor but if we put the calculations outside
				//the synchronized block then we get a bigger delay in the 
				//realtime plot...I think it may be better here because
				//opcom does not need the monitor to update the plot anyway
				mon.updateState(uAngle);
			}
		}
		mutex.give();
		
	}

}
