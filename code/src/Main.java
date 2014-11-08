import javax.swing.SwingUtilities;

public class Main {
	public static void main(String args[]){
		
		final int regulPriority = 8;
		final int plotterPriority = 7;
		final int switchPriority = 5;
		final int refGenPriority = 6;
		
		ReferenceGenerator refgen = new ReferenceGenerator(refGenPriority);
		Monitor mon = new Monitor();
		final OpCom opcom = new OpCom(plotterPriority, mon);
		RegulThread regThread= new RegulThread(mon, regulPriority);
		SwitchThread switchThread = new SwitchThread(mon, switchPriority);
		
		regThread.setOpCom(opcom);
		regThread.setRefGen(refgen);
		opcom.setRegul(regThread); 
		
		/** By doing this initializeGUI() is done in the event-dispatcher thread
		 *  see "Swing Thread Safety" in Exercise 4 */
		Runnable initializeGUI = new Runnable(){
			public void run(){
			    opcom.initializeGUI();
			    opcom.start();
			}
		};
		try{
		    SwingUtilities.invokeAndWait(initializeGUI);
		}catch(Exception e){
		    return;
		}

		regThread.start();
		switchThread.start();
		
	}
}
