package paper.semantic.matrix;

import weka.core.matrix.Matrix;


public class JamaMath {
	public static void main(String[] args) throws Exception{
		double[][] rawMatrix1 = {{1, 2},
								{1, 3}};
		double[][] rawMatrix2 = {{1,1},
								{0, 1}};
		double[][] matrix = getTimesMatrix(rawMatrix1, rawMatrix2);
		showMatrix(matrix);
		
	}
	
	public static double[][] getTimesMatrix(double[][] rawMatrix1, double[][] rawMatrix2){
		Matrix matrix1 = new Matrix(rawMatrix1);
		Matrix matrix2 = new Matrix(rawMatrix2);
		Matrix matrix = matrix1.times(matrix2);
		return matrix.getArray();
	}
	
	public static void showMatrix(double[][] matrix){
		for(int i=0; i<matrix.length; i++){
			for(int j=0; j<matrix[i].length; j++){
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
		}
	}
}
