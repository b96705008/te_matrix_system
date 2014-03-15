package paper.semantic.matrix;

import  weka.core.matrix.Matrix;
import  weka.core.matrix.SingularValueDecomposition;

/**
     * This class implements the Latent Semantic Indexing technique. <br/>
     * LSI generalises a sparse matrix of indexed terms using term co-occurrences.
     * The approximation uses a given lower rank of the matrix or a ratio of the S values gotten from the SVD.
     * <p> It should be noted that the number of S values is NOT always equal to the Rank of the matrix. 
     * <p>It uses a third party Java package called Jama for Singular Value Decomposition.
     * @author Ibrahim Adeyanju
     * assisted by Stewart Massie
     * @version 1.0
*/
public class LSI {

	/**
	 * @param args
	 * A = original 	
	 * D = document
	 * S = singular
	 * T = term
	 */
	public static void main(String[] args) {
		double[][] cb = {{1,0,0,1,0,0,0,0,0}, 
						{1,0,1,0,0,0,0,0,0},
						{1,1,0,0,0,0,0,0,0},
						{0,1,1,0,1,0,0,0,0},
						{0,1,1,2,0,0,0,0,0},
						{0,1,0,0,1,0,0,0,0},
						{0,1,0,0,1,0,0,0,0},
						{0,0,1,1,0,0,0,0,0},
						{0,1,0,0,0,0,0,0,1},
						{0,0,0,0,0,1,1,1,0},
						{0,0,0,0,0,0,1,1,1},
						{0,0,0,0,0,0,0,1,1}};
		
		LSI lsi = new LSI(cb, 2);
		lsi.getAk();
		lsi.getD_D();
		lsi.getT_T();
	}
	
	//property
	 private Matrix A; 
	 private Matrix Ak;
	 private Matrix D_D;
	 private Matrix T_T;
	 private Matrix S;
	 private int k; 
	 
	 /**
	  * Constructor to instantiate a new LSI class. 
	  * The default number of terms used for approximation is half of the matrix Rank
	  * @param cb A 2-dimensional array intended to be generalised 
	  */
	 public LSI(double[][] cb){
		 this.A = new Matrix(cb);
		 this.k = (int)(A.rank() * 0.5);
	 }
	 
	 /**
	 * Constructor to instantiate a new LSI class
	 * @param cb A 2-dimensional array intended to be generalised 
	 * @param k An integer representing the rank of approximation
	 */
	 public LSI(double[][] cb, int k){
		 this.A = new Matrix(cb);
		 this.k = k;
	 }
	 
	 /**
	 * Constructor to instantiate a new LSI class
	 * @param cb A 2-dimensional array intended to be generalised 
	 * @param ratio A fraction representing a ratio of the number of S values used for approximation
	 */
	 public LSI(double[][] cb, double ratio){
		 this.A = new Matrix(cb);
		 int row = this.A.getRowDimension();
		 int col = this.A.getColumnDimension();
		 if(row < col)
			this.k = (int)(ratio * row);
		 else
			this.k = (int)(ratio * col); 
	 }
	 
	 /**
	 * Gets the Generalised matrix
	 * @return Generalised matrix
	 */
	 public double[][] getAk(){
		 boolean transpose = false;
		 
		 //if rows < cols then transpose A
		 if(this.A.getRowDimension() < this.A.getColumnDimension()){
			 this.A = this.A.transpose();
			 transpose = true;
		 }
		 
		 //get S V D matrices
		 SingularValueDecomposition svd = new SingularValueDecomposition(this.A);
		 
		 Matrix U = svd.getU();
		 this.S = svd.getS();
		 Matrix V = svd.getV();
		 
		 // trim the matrices according to k
		 Matrix Uk = U.getMatrix(0, U.getRowDimension() - 1, 0, this.k - 1);
		 Matrix Sk = this.S.getMatrix(0, this.k - 1, 0, this.k - 1);
		 Matrix Vk = V.getMatrix(0, V.getRowDimension() - 1, 0, this.k - 1);
		 
		 //reconstruct matrix at lower rank
		 if(transpose)
			 this.Ak = (Uk.times(Sk)).times(Vk.transpose()).transpose();
		 else
			 this.Ak = (Uk.times(Sk)).times(Vk.transpose());
		 
		 return this.Ak.getArrayCopy();
	 }
	 
	 /**
	 * Gets the similarity between elements in the rows of the given matrix
	 * @return Matrix of similarity between row elements
	 */
	 public double[][] getD_D(){
		 if(this.Ak == null)
			 this.getAk();
		 
		 //doc/doc cosine similarity - normalised dot product
		 // The given matrix is assumed to be a document by term matrix
		 this.D_D = this.Ak.times(this.Ak.transpose());
		 
		 //set normalise factor for each row vector i.e. 2norm
		 Matrix norms = new Matrix(this.Ak.getRowDimension(), 1);
		 for(int i=0; i<this.Ak.getRowDimension(); i++){
			 double val1 = Math.sqrt(this.D_D.get(i,i));
			 if(val1 == 0) val1 = 0.000001;
			 norms.set(i, 0, val1);
		 }
		 
		  //set cosine similarity dot prod/norm factor for each element
		 for (int i = 0; i < this.D_D.getRowDimension(); i++){
			 for (int a = 0; a < this.D_D.getColumnDimension(); a++){
				 double val = this.D_D.get(i,a) / (norms.get(i,0)*norms.get(a,0));
				 this.D_D.set(i,a,val);
			 }
		 }
		 this.D_D.print(5, 2);
		 return this.D_D.getArray();
	 }
	 
	 /**
	 * Gets the similarity between elements in the columns of the given matrix
	 * @return Matrix of similarity between column elements
	 */

	 public double[][] getT_T(){
		 if(this.Ak==null) this.getAk();
		 
		 //term/term cosine similarity   - normalised dot product
		 //The given matrix is assumed to be a document by term matrix
		 Matrix AkT = this.Ak.transpose();
		 this.T_T=AkT.times(this.Ak);
		 
		 //set normalise factor for each row vector i.e. 2norm
		 Matrix norms = new Matrix(AkT.getRowDimension(),1);
		 for (int i = 0; i < AkT.getRowDimension(); i++) {
			 double val1 = Math.sqrt(this.T_T.get(i,i));
			 if(val1 == 0) val1 = 0.000001;
			 norms.set(i,0,val1);
		 }
		 
		 //set cosine similarity dot prod/norm factor for each element
		 for (int i = 0; i < this.T_T.getRowDimension(); i++) {
			 for (int a = 0; a < this.T_T.getColumnDimension(); a++) {
				 double val = this.T_T.get(i,a)/(norms.get(i,0)*norms.get(a,0));
				 this.T_T.set(i,a,val);
			 }
		 }
		 this.T_T.print(5, 2);
		 return this.T_T.getArrayCopy();
	 }


	/**
	 * Gets the diagonal matrix of S values after SVD
	 * @return Diagonal matrix of S values
	 */
	 public double[][] getS(){
		 if(this.Ak == null) this.getAk();
		 return this.S.getArrayCopy();
	 }

	 /**
	 * Gets the rank of the Original Matrix
	 * @return Rank of the original matrix
	 */
	 public int getRank(){   
		 return this.A.rank();
	 }
}
