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
	private long startTime;
	
	//input and output analog signals to and from the real process
	private AnalogIn analogInAngle;        // angle of the beam = yAngle
	private AnalogIn analogInPosition;     // position of the ball = yPos
	private AnalogOut analogOut;           // torque for the beam = uAngle
	
	private double uAngle;
	private double[] ref = new double[4];
	private double[] measurement;  //yAngle on index 0, yPos on index 1
	
	/**
	 * Conversion of measurements from Volt to SI units
	 * TEMPORARY NUMBERS 
	 */
	private static final double radiansPerVolt = 3.1415926535897932384626d / 4 / 10;
	private static final double angleBiasVolt = 0.0;
	private static final double metersPerVolt = 0.55 / 10;
	private static final double positionBiasVolt = 0.0;

	
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
		
		measurement = new double[2]; 
		mon = monitor;
		mutex = new Semaphore(1);
		setPriority(prio);
	}
	
	/** called from Main */
	public void setOpCom(OpCom opcom) {
		this.opcom = opcom;
	}
	
	private void sendDataToOpCom(double[] yref, double[] y, double u) {
		double x = (double)(System.currentTimeMillis() - startTime) / 1000.0;
		DoublePoint dp = new DoublePoint(x,u);
		PlotData pd = new PlotData(x, yref[ReferenceGenerator.ANGLE], y[0]);
		PlotData pd2 = new PlotData(x, yref[ReferenceGenerator.POS], y[1]);
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
			
			//get the angle of the beam and its set point
			try {
				measurement[0] = radiansPerVolt*(analogInAngle.get() - angleBiasVolt);
			} catch (IOChannelException e) {
				e.printStackTrace();
			}
			
			//get the position of the ball
			try {
				measurement[1] = metersPerVolt*(analogInPosition.get() - positionBiasVolt);
			} catch (IOChannelException e) {
				e.printStackTrace();
			}
						
			synchronized(mon){ //to get synchronization between calcOutput and updateState
				uAngle = mon.calcOutput(measurement);
			
				//send the control signal to the real process
				try {
					analogOut.set(uAngle);
				} catch (IOChannelException e) {
					e.printStackTrace();
				}
				
				//might have to rethink this part...
				ref = mon.getRef();

				sendDataToOpCom(ref, measurement, uAngle);
				
				//if we do updateState here then this above calculation
				//is done while holding the monitor but if we put the calculations outside
				//the synchronized block then we get a bigger delay in the 
				//realtime plot...I think it may be better here because
				//opcom does not need the monitor to update the plot anyway
				mon.updateState();
			}
			
			mon.checkState();
			
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
