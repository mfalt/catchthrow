
public abstract class ScalarRef extends ReferenceGenerator {
	protected int actualState = ANGLE; // Beam angle is standard state for reference

	public void pickState(int newState) {
		ref[actualState] = 0.0;
		actualState = newState;
	}

}
