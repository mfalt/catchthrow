package checker;



public interface StateChecker {
	/**
	 * Checks state of system (in some way) to determine if state is "OK" enough
	 * to proceed in sequence
	 * 
	 * @param y
	 *            measured values
	 * @return true if state is OK, false otherwise
	 */
	public boolean check(double[] y);
}