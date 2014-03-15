package paper.semantic;

import mdsj.MDSJ;

public class MDSTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[][] input={        // input dissimilarity matrix
			    {0.00,2.04,1.92,2.35,2.06,2.12,2.27,2.34,2.57,2.43,1.90,2.41},
			    {2.04,0.00,2.10,2.00,2.23,2.04,2.38,2.36,2.23,2.36,2.57,2.34},
			    {1.92,2.10,0.00,1.95,2.21,2.23,2.32,2.46,1.87,1.88,2.41,1.97},
			    {2.35,2.00,1.95,0.00,2.05,1.78,2.08,2.27,2.14,2.14,2.38,2.17},
			    {2.06,2.23,2.21,2.05,0.00,2.35,2.23,2.18,2.30,1.98,1.74,2.06},
			    {2.12,2.04,2.23,1.78,2.35,0.00,2.21,2.12,2.21,2.12,2.17,2.23},
			    {2.27,2.38,2.32,2.08,2.23,2.21,0.00,2.04,2.44,2.19,1.74,2.13},
			    {2.34,2.36,2.46,2.27,2.18,2.12,2.04,0.00,2.19,2.09,1.71,2.17},
			    {2.57,2.23,1.87,2.14,2.30,2.21,2.44,2.19,0.00,1.81,2.53,1.98},
			    {2.43,2.36,1.88,2.14,1.98,2.12,2.19,2.09,1.81,0.00,2.00,1.52},
			    {1.90,2.57,2.41,2.38,1.74,2.17,1.74,1.71,2.53,2.00,0.00,2.33},
			    {2.41,2.34,1.97,2.17,2.06,2.23,2.13,2.17,1.98,1.52,2.33,0.00}
		        };
			
			int n=input[0].length;    // number of data objects
			//double[][] coutput = MDSJ.classicalScaling(input); // apply MDS
			double[][] output = MDSJ.stressMinimization(input, 3);
			double[][] matrix = new double[n][output.length];
			
			for(int i=0; i<n; i++){
				for(int j=0; j<output.length; j++){
					matrix[i][j] = output[j][i];
					//System.out.print(output[j][i]+" ");
				}
				//System.out.println();
			}
			
			for(int i=0; i<n; i++){
				for(int j=0; j<matrix[i].length; j++){
					System.out.print(matrix[i][j]+" ");
				}
				System.out.println();
			}
		
	}

}
