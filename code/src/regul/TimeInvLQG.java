package regul;

import java.io.FileNotFoundException;
import java.io.IOException;

import refgen.ReferenceGenerator;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * LQG control for control to a specific state, around which the model is linearized
 */
public class TimeInvLQG extends Regul {
	private int N; // Number of states
	private int M; // Number of outputs (measurements)
	private int uDim = 1; // Number of inputs, hard coded
	
	private MLDouble Phi; // N x N
	private MLDouble Gamma; // N x uDim (one input)
	private MLDouble C; // M x N
	private MLDouble D; // M x uDim
	
	private MLDouble L; // uDim x N
	private MLDouble K; // N x M
	
	private MLDouble x0; // Linearization point
	
	// Signals
	private double u; // scalar
	private double[] y; // Mx1
	private double[] xtildehat; // Nx1, estimated state. xtildehat = xhat - x0 
	
	/**
	 * 
	 * @param linPoint
	 */
	public TimeInvLQG(String file) throws FileNotFoundException, IOException {
		MatFileReader matFileReader = new MatFileReader(file);
//		MLArray hMLArray = matFileReader.getMLArray("h");
		MLArray PhiMLArray = matFileReader.getMLArray("Phi");
		MLArray GammaMLArray = matFileReader.getMLArray("Gamma");
		MLArray CMLArray = matFileReader.getMLArray("C");
		MLArray DMLArray = matFileReader.getMLArray("D");
		MLArray LMLArray = matFileReader.getMLArray("L");
		MLArray KMLArray = matFileReader.getMLArray("K");
		MLArray x0MLArray = matFileReader.getMLArray("x0");
		
		N = ReferenceGenerator.nbrStates;
		M = CMLArray.getM();
		
		// Validate input
		if(
				PhiMLArray.isEmpty() || PhiMLArray.getM() != N || PhiMLArray.getN() != N || PhiMLArray.isComplex() || !PhiMLArray.isDouble() ||
				GammaMLArray.isEmpty() || GammaMLArray.getM() != N || GammaMLArray.getN() != uDim || GammaMLArray.isComplex() || !GammaMLArray.isDouble() ||
				CMLArray.isEmpty() || CMLArray.getM() != M || CMLArray.getN() != N || CMLArray.isComplex() || !CMLArray.isDouble() ||
				DMLArray.isEmpty() || DMLArray.getM() != M || DMLArray.getN() != uDim || DMLArray.isComplex() || !DMLArray.isDouble() ||
				LMLArray.isEmpty() || LMLArray.getM() != uDim || LMLArray.getN() != N || LMLArray.isComplex() || !LMLArray.isDouble() ||
				KMLArray.isEmpty() || KMLArray.getM() != N || KMLArray.getN() != M || KMLArray.isComplex() || !KMLArray.isDouble() ||
				x0MLArray.isEmpty() || x0MLArray.getM() != N || x0MLArray.getN() != 1 || x0MLArray.isComplex() || !x0MLArray.isDouble()
				) {
			throw new IllegalArgumentException("Invalid .mat file!");
		}
		
		Phi = (MLDouble) PhiMLArray;
		Gamma = (MLDouble) GammaMLArray;
		C = (MLDouble) CMLArray;
		D = (MLDouble) DMLArray;
		L = (MLDouble) LMLArray;
		K = (MLDouble) KMLArray;
		x0 = (MLDouble) x0MLArray;
	}
	
	/**
	 * If only controlling to the predefined linearization point with a predefined sample period is allowed,
	 * the ref and h arguments are unnecessary here.
	 */
	public double calculateOutput(double[] measurement, double[] ref, double h) {
		this.y = measurement.clone(); // Clone really necessary?
		u = 0;
		for(int i = 0; i < N; ++i) {
			u -= L.get(i)*xtildehat[i];
		}
		return u;
	}

	/**
	 * DO NOT FORGET TO ACCOUNT FOR LINEARIZATION POINT! WHAT DOES XHAT==0 CORRESPOND TO?
	 */
	public void updateState(double h) {
		// Compute residuals
		// epsilon =  y - C*x - D*u
		double epsilon[] = y.clone(); // Clone necessary?
		for(int i = 0; i < N; ++i) {
			for(int j = 0; j < M; ++j) {
				epsilon[j] -= (C.get(j, i)*xtildehat[i] + D.get(j, 0)*u); // Residual of first measurement 
			}
		}
		
		// Update states
		// xhat = Phi*oldxhat + Gamma*u + K*epsilon
		double oldxhat[] = xtildehat.clone(); // Clone necessary?
		for(int i = 0; i < N; ++i) { // Set xtildehat to 0 before adding other stuff
			xtildehat[i] = 0.0;
		}
		for(int i = 0; i < N; ++i) { // Loop through the states
			// First term, Phi*oldxhat
			for(int j = 0; j < N; ++j) {
				xtildehat[i] += Phi.get(i, j)*oldxhat[j];
			}
			
			// Second term, Gamma*u
			xtildehat[i] += Gamma.get(i)*u;
			
			// Third term, K*epsilon
			for(int j = 0; j < M; ++j) { // two outputs
				xtildehat[i] += K.get(i, j)*epsilon[j];
			}
		}
	}
	
	/**
	 * 
	 * @param xhat desired initial value of states (actual states, xhat != 0 at lin point, but instead xhat == x0) 
	 */
	public void reset(double[] xhat) {
		xtildehat = xhat.clone();
		for(int i = 0; i < N; ++i) {
			xtildehat[i] -= x0.get(i);
		}
	}

}
