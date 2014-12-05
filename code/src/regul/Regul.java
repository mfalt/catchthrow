package regul;


public abstract class Regul{
	
	public abstract double calculateOutput(double[] measurement, double[] ref, double h);
	
	public abstract void updateState(double h);
	
	public abstract void reset(double[] states);
	
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

