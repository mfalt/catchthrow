import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;

/** This class is the same as the one from lab 1*/
public class ReferenceGenerator extends Thread {
	private static final int SQWAVE=0, MANUAL=1;
	private final int priority;
	
	private double amplitude;
	private double period;
	private double sign = -1.0;
	private double ref;
	private double manual;
	private int mode = SQWAVE;
	private boolean premature, ampChanged, periodChanged;
	
	private class RefGUI {
		private BoxPanel guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		private JPanel sliderPanel = new JPanel();
		private JPanel paramsLabelPanel = new JPanel();
		private JPanel paramsFieldPanel = new JPanel();
		private BoxPanel paramsPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		private BoxPanel parAndButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		private BoxPanel buttonsPanel = new BoxPanel(BoxPanel.VERTICAL);
		private JPanel rightPanel = new JPanel();
		
		private DoubleField paramsAmpField = new DoubleField(7,5);
		private DoubleField paramsPeriodField = new DoubleField(7,5);
		private JButton paramsButton = new JButton("Apply");
		private JRadioButton sqButton = new JRadioButton("Square Wave");
		private JRadioButton manButton = new JRadioButton("Manual");
		private JSlider slider = new JSlider(JSlider.VERTICAL,-50,50,0);
		
		public RefGUI(double amp, double h) {
			MainFrame.showLoading();
			paramsLabelPanel.setLayout(new GridLayout(0,1));
			paramsLabelPanel.add(new JLabel("Amp: "));
			paramsLabelPanel.add(new JLabel("Period: "));
			
			paramsFieldPanel.setLayout(new GridLayout(0,1));
			paramsFieldPanel.add(paramsAmpField); 
			paramsFieldPanel.add(paramsPeriodField);   
			paramsPanel.add(paramsLabelPanel);
			paramsPanel.addGlue();
			paramsPanel.add(paramsFieldPanel);
			paramsPanel.addFixed(10);
			paramsAmpField.setValue(amp);
			paramsAmpField.setMaximum(10.0);
			paramsAmpField.setMinimum(0.0);
			paramsPeriodField.setValue(h);
			paramsPeriodField.setMinimum(0.0);
			
			parAndButtonPanel.setBorder(BorderFactory.createEtchedBorder());
			parAndButtonPanel.addFixed(10);
			parAndButtonPanel.add(paramsPanel);
			paramsPanel.addFixed(10);
			parAndButtonPanel.add(paramsButton);
			paramsButton.setEnabled(false);
			
			buttonsPanel.setBorder(BorderFactory.createEtchedBorder());
			buttonsPanel.add(sqButton);
			buttonsPanel.addFixed(10);
			buttonsPanel.add(manButton);
			ButtonGroup group = new ButtonGroup();
			group.add(sqButton);
			group.add(manButton);
			sqButton.setSelected(true);
			
			rightPanel.setLayout(new BorderLayout());
			rightPanel.add(parAndButtonPanel, BorderLayout.CENTER);
			rightPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			slider.setPaintTicks(true);
			slider.setEnabled(false);
			slider.setMajorTickSpacing(5); 
			slider.setMinorTickSpacing(1); 
			slider.setLabelTable(slider.createStandardLabels(10)); 
			slider.setPaintLabels(true);
			sliderPanel.setBorder(BorderFactory.createEtchedBorder());
			slider.setSnapToTicks(false);
			sliderPanel.add(slider);
			
			guiPanel.add(sliderPanel);
			guiPanel.addGlue();
			guiPanel.add(rightPanel);
			
			paramsAmpField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ampChanged = true;
					paramsButton.setEnabled(true);
				}
			});
			paramsPeriodField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					periodChanged = true;
					paramsButton.setEnabled(true);
				}
			});  
			paramsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (ampChanged) {
						amplitude = paramsAmpField.getValue();
						ampChanged = false;
						setRef(sign * amplitude);
					}
					if (periodChanged) {
						period = paramsPeriodField.getValue()*1000.0/2.0;
						periodChanged = false;
						wakeUpThread();
					}
					paramsButton.setEnabled(false);
				}
			});
			sqButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSqMode();
					paramsAmpField.setEditable(true);
					paramsPeriodField.setEditable(true);
					slider.setEnabled(false);
				}
			});
			manButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setManMode();
					paramsAmpField.setEditable(false);
					paramsPeriodField.setEditable(false);
					slider.setEnabled(true);
				}
			});
			slider.addChangeListener(new ChangeListener() { 
				public void stateChanged(ChangeEvent e) { 
					if (!slider.getValueIsAdjusting()) { 
						setManual(((double)slider.getValue())/5); 
					} 
				} 
			}); 
			
			MainFrame.setPanel(guiPanel,"RefGen");
		}
	}
	
	public ReferenceGenerator(int refGenPriority) {
		priority = refGenPriority;
		amplitude = 4.0;
		period = 20.0*1000.0/2.0;
		manual = 0.0;
		ref = amplitude * sign;
		new RefGUI(4.0, 20.0);
	}
	
	private synchronized void wakeUpThread() {
		premature = true;
		notify();
	}
	
	private synchronized void sleepLight(long duration) throws InterruptedException {
		premature = false;
		wait(duration);
	}
	
	private synchronized void setRef(double newRef) {
		ref = newRef;
	}
	
	private synchronized void setManual(double newManual) {
		manual = newManual;
	}
	
	private synchronized void setSqMode() {
		mode = SQWAVE;
	}
	
	private synchronized void setManMode() {
		mode = MANUAL;
	}
	
	public synchronized double getRef() 
	{
		return (mode == SQWAVE) ? ref : manual;
	}
	
	public void run() {
		long h = (long) period;
		long duration;
		long t = System.currentTimeMillis();
		
		setPriority(priority);
		
		try {
			while (!isInterrupted()) {
				synchronized (this) {
					sign = - sign;
					ref = amplitude * sign;
				}
				t = t + h;
				duration = t - System.currentTimeMillis();
				if (duration > 0) {
					sleepLight(duration);
					if (premature) {
						// Woken up prematurely since the period was changed
						h = (long) period;
						// Reset t
						t = System.currentTimeMillis();
						// Keep current sign 
						sign = - sign;
					}
				}
			}
		} catch (InterruptedException e) {
			// Requested to stop
		}
	}
}

