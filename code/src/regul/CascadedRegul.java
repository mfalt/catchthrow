package regul;

public class CascadedRegul extends Regul {
	
	private Regul innerRegul;
	private Regul outerRegul;
	private int state;

	/**
	 * 
	 * @param innerRegul
	 * @param outerRegul
	 * @param state - the reference state to which the outer controller will apply its control signal
	 */
	public CascadedRegul(Regul innerRegul, Regul outerRegul, int state) {
		this.innerRegul = innerRegul;
		this.outerRegul = outerRegul;
		this.state = state;
	}
	
	@Override
	public double calculateOutput(double[] measurement, double[] ref, double h) {
		double outerControlSignal = outerRegul.calculateOutput(measurement, ref, h);
		double[] innerRef = ref.clone();
		innerRef[state] += outerControlSignal;
		return innerRegul.calculateOutput(measurement, innerRef, h);
	}

	@Override
	public void updateState(double h) {
		innerRegul.updateState(h);
		outerRegul.updateState(h);
	}

	/**
	 * RESET HAS TO BE FIXED! LET ALL CONTROLLERS GET AL STATES AS INPUTS TO THE RESET METHOD.
	 */
	@Override
	public void reset(double[] states) {
		outerRegul.reset(states);
		innerRegul.reset(states);
	}

}
