public class BeamRegul extends Regul {

	Regul nextRegul;
	
	public BeamRegul() {
		// TODO Auto-generated constructor stub
	}

	public OutSignal calcOut(Measurement measurement) {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(long t0, Regul prevRegul) {
		//Obs handle null prevRegul
		finishChecker = new RegulCheckFinished();
		//Maybe not recreate thread
		finishChecker.start();
	}

	public void updateState() {
		// TODO Auto-generated method stub
	}
	
	public void setNextRegul(Regul r) {
		nextRegul = r;
	}

	public boolean checkFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	public Regul getNextRegul() {
		return nextRegul;
	}

}