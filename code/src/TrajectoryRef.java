import java.io.FileNotFoundException;
import java.io.IOException;

import com.jmatio.io.MatFileReader;


public class TrajectoryRef extends ReferenceGenerator {

	public TrajectoryRef(String file) throws FileNotFoundException, IOException {
		MatFileReader matFileReader = new MatFileReader(file);
	}
	
	@Override
	public double getRef() {
		return 0;
	}

}
