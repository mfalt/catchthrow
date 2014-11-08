
public abstract class Regul{
	
	public abstract double calculateOutput(double[] y, double yref);
	
	public abstract void updateState(double u);
	
	public abstract long getHMillis();
	
	
	
}
	/*Main main;
	long t0;
	Thread finishChecker;

	//Obs handle null prevRegul
	public abstract void init(long t0, Regul prevRegul);
	
	public abstract boolean checkFinished();
	
	public abstract Regul getNextRegul();
	
	public class RegulCheckFinished extends Thread {
		public void run() {
			while(!checkFinished()){}
			main.switchRegul(getNextRegul());
		}
	}
*/

