
public class SwitchThread extends Thread {
	Monitor mon;
	
	/** Constructor */
	public SwitchThread(Monitor monitor, int prio) {
		mon = monitor;
		setPriority(prio);
	}
	
	
	
	public void run() {
		int mode;
		double[] latestBeamAngles = new double[3];
		double average, sum = 0;
		double epsilon = 0.5;
		
		while(!isInterrupted()) {
			mode = mon.getMode();
			latestBeamAngles = mon.getLatestBeamAngles();
			
			for(int i = 0; i < 3; ++i){
				sum += latestBeamAngles[i];
			}
			average = sum/3;
			
			//we know we are in beam mode and the beam angle has been
			//relatively constant around zero
			// Change to ball mode when the sensor triggers instead? //Lucas
			if(mode == Monitor.BEAM && average > epsilon && average < 1) {
				mon.setBallMode(); //i.e switch to ball
				try {
					sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//to be continued
		}
	}
}
