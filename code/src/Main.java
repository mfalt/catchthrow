
public class Main extends Thread {

	BeamRegul beam;
	BeamBallRegul beamBall;
	BeamBallRegul beamBallThrowSmall;
	BeamBallRegul beamBallThrowMedium;
	BeamBallRegul beamBallThrowBig;

	private Regul currentRegul;

	public static void main(String args[]){
		Main main = new Main();
		main.start();
	}

	public Main(){
		beam = new BeamRegul();
		//Create other reguls
		beam.setNextRegul(beamBall);
		//Set the rest of the next
		//Do something?
		switchRegul(beam);
	}

	public synchronized void switchRegul(Regul r){
		Regul prevRegul = currentRegul;
		currentRegul = r;
		r.init(System.currentTimeMillis(), prevRegul);
		//r.start()?
	}

	public void run(){
		long t = System.currentTimeMillis();
		long h = 10;
		Measurement measurement;
		while(true){
			measurement = new Measurement(/*Get input*/);
			synchronized (this) {
				currentRegul.calcOut(measurement);
				// Set output
				currentRegul.updateState();
			}
			long thist = System.currentTimeMillis();
			if(thist < t+h){
				try{
					Thread.sleep(t+h-thist);
				}catch (Exception e) {
					System.exit(1);
				}
			}
			t += h;
		}
	}
}
