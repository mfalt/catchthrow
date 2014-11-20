
public class SwitchThread extends Thread {
	Monitor mon;
	
	/** Constructor */
	public SwitchThread(Monitor monitor, int prio) {
		mon = monitor;
		setPriority(prio);
	}
	
	
	
	public void run() {
		
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
			
		//}
		
		
	}
}
