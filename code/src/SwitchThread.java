import se.lth.control.realtime.Semaphore;


public class SwitchThread extends Thread {
	private Monitor mon;
	private boolean shouldRun = true; 
	private Semaphore sem;
	
	/** Constructor */
	public SwitchThread(Monitor monitor, Semaphore sem, int prio) {
		mon = monitor;
		setPriority(prio);
		this.sem = sem;
	}
	
	
	
	public void run() {
		while(shouldRun){
			sem.take();
			if(!shouldRun){
				break;
			}
			
			
			
		/*Test change of reference values!
		mon.setBeamMode();
		try {
			mon.setRefGenConstant(2.0);
			sleep(2000);
			mon.setRefGenRamp(-1.0);
			sleep(2000);
			mon.setRefGenConstant(-1.0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//while(!isInterrupted()){
			
		//}// TODO Auto-generated method stub
			
		}
		
	}



	public void shutdown() {
		shouldRun = false;
	}
}
