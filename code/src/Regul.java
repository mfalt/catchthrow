
public abstract class Regul{
	Main main;
	long t0;
	Thread finishChecker;
	
	public abstract OutSignal calcOut(Measurement measurement);
	
	//Obs handle null prevRegul
	public abstract void init(long t0, Regul prevRegul);
	
	public abstract void updateState();
	
	public abstract boolean checkFinished();
	
	public abstract Regul getNextRegul();
	
	public class RegulCheckFinished extends Thread {
		public void run() {
			while(!checkFinished()){}
			main.switchRegul(getNextRegul());
		}
	}

}
