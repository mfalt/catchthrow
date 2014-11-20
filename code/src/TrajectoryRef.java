import java.io.IOException;

import com.jmatio.io.MatFileReader;


public class TrajectoryRef extends ReferenceGenerator {

	public TrajectoryRef(String file) {
		try {
			MatFileReader fileReader = new MatFileReader(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public double getRef() {
		return 0;
	}

}
