
public class SwitchThread extends Thread {
	Monitor mon;
	
	/** Constructor */
	public SwitchThread(Monitor monitor, int prio) {
		mon = monitor;
		setPriority(prio);
	}
	
	
	
	public void run() {
		
		//mon.setBeamMode();
		
		//while(!isInterrupted()){
			
		//}
		
		
	}
}
