package main;

import javax.swing.SwingUtilities;

import se.lth.control.realtime.Semaphore;

import refgen.*;


public class Main {
	public static void main(String args[]){
		
		final int regulPriority = 8;
		final int plotterPriority = 6;
		final int switchPriority = 7;
		final int refGenPriority = 6;
		
		RefGenGUI refGenPos = new RefGenGUI(refGenPriority, ReferenceGenerator.POS, "Position RefGen");
		RefGenGUI refGenAngle = new RefGenGUI(refGenPriority, ReferenceGenerator.ANGLE, "Angle RefGen");
		Monitor mon = new Monitor();
		Semaphore switchThreadSem = new Semaphore(1);
		final OpCom opcom = new OpCom(plotterPriority, mon,switchThreadSem);
		RegulThread regThread= new RegulThread(mon, regulPriority);
		SwitchThread switchThread = new SwitchThread(mon, switchThreadSem, switchPriority);
		
		regThread.setOpCom(opcom);
		mon.initRefGenGUI(refGenPos, refGenAngle);
		opcom.setRegul(regThread); 
		opcom.setSwitchThread(switchThread);
		
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
//		refgen.start();
		new Thread(refGenPos).start();
		new Thread(refGenAngle).start();
		
	}
}
