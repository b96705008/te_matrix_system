package paper.semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

//利用WordNet similarity來建立term similarity matrix
public class TermMapping {

	public static void main(String[] args) {
		Set<String> termSet = new TreeSet<String>();
		termSet.add("detector");
		termSet.add("detection");
		termSet.add("sensor");
		termSet.add("layer");
		
		TermMapping tm = new TermMapping(termSet);
		tm.getSimMatrix();
		
	}
	
	//property
	private List<String> termList;
	private double[][] simMatrix;
	
	//constructor
	public TermMapping(Set<String> termSet){
		this.termList = new ArrayList<String>();
		this.termList.addAll(termSet);
		this.generateSimMatrix();
	}
	
	//method
	public int getTermSize(){
		return this.termList.size();
	}
	
	public String getTermByIndex(int index){
		return this.termList.get(index);
	}
	
	public List<String> getTermList(){
		return this.termList;
	}
	
	private void generateSimMatrix(){
		System.err.println("generate term Sim Matrix...");
		
		int n = this.getTermSize();
		this.simMatrix = new double[n][n];
		
		for(int i=0; i<n; i++){
			for(int j=i; j<n; j++){
				if(i == j) 
					this.simMatrix[i][j] = 1;
				else{
					String termi = this.getTermByIndex(i);
					String termj = this.getTermByIndex(j);
					double sim = WordNetSim.wnsimSimilarity(termi, termj);
					this.simMatrix[i][j] = sim;
					this.simMatrix[j][i] = sim;
				}			
			}//end for
		}//end for
	}
	
	public double[][] getSimMatrix(){
		return this.simMatrix;
	}
}
