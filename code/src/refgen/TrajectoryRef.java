package refgen;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

public class TrajectoryRef extends ReferenceGenerator {

	private int N;
	private double h;
	private MLDouble posRef;
	private MLDouble velRef;
	private MLDouble angleRef;
	private MLDouble angleVelRef;
	private int currentArrayIdx = 0;
	
	public TrajectoryRef(String file) throws FileNotFoundException, IOException {
		MatFileReader matFileReader = new MatFileReader(file);
		MLArray hMLArray = matFileReader.getMLArray("h");
		MLArray posRefMLArray = matFileReader.getMLArray("posRef");
		MLArray velRefMLArray = matFileReader.getMLArray("velRef");
		MLArray angleRefMLArray = matFileReader.getMLArray("angleRef");
		MLArray angleVelRefMLArray = matFileReader.getMLArray("angleVelRef");
		
		// Validate input
		if(
				hMLArray.isComplex() || !hMLArray.isDouble() || hMLArray.isEmpty() ||
				posRefMLArray.isComplex() || !posRefMLArray.isDouble() || posRefMLArray.isEmpty() ||
				velRefMLArray.isComplex() || !velRefMLArray.isDouble() || velRefMLArray.isEmpty() ||
				angleRefMLArray.isComplex() || !angleRefMLArray.isDouble() || angleRefMLArray.isEmpty() ||
				angleVelRefMLArray.isComplex() || !angleVelRefMLArray.isDouble() || angleVelRefMLArray.isEmpty() ||
				hMLArray.getSize() != 1
				) {
			throw new IllegalArgumentException("Invalid .mat file!");
		}
		
		N = Math.min(Math.min(Math.min(posRefMLArray.getSize(), velRefMLArray.getSize()), angleRefMLArray.getSize()), angleVelRefMLArray.getSize()); // Correct to use getSize?
		h = ((MLDouble) hMLArray).get(0);
		posRef = (MLDouble) posRefMLArray;
		velRef = (MLDouble) velRefMLArray;
		angleRef = (MLDouble) angleRefMLArray;
		angleVelRef = (MLDouble) angleVelRefMLArray;
	}
	
	//@Override
	public double[] getRef() {
		//updateReferences();
		return ref;
	}

	private void updateReferences() {
		currentArrayIdx = Math.min((int) Math.floor(getTimeSeconds() / h), N-1);
		ref[0] = posRef.get(currentArrayIdx);
		ref[1] = velRef.get(currentArrayIdx);
		ref[2] = angleRef.get(currentArrayIdx);
		ref[3] = angleVelRef.get(currentArrayIdx);
	}

}
