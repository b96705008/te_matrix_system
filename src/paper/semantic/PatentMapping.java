package paper.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mdsj.MDSJ;

import paper.patent.Patent;
import paper.patent.PatentGroup;

//用來處理patent間相似度矩陣, 或組合專利vector成矩陣的類別
public class PatentMapping {

	public static void main(String[] args) {

	}
	
	//property
	private PatentGroup patentGroup;
	private List<String> patentIDList;
	
	//constructor
	public PatentMapping(PatentGroup patentGroup){
		this.patentGroup = patentGroup;
		this.patentIDList = this.patentGroup.getPatentIDList();
	}
	
	public PatentMapping(PatentGroup patentGroup, TreeSet<String> patentIDs){
		this.patentGroup = patentGroup;
		this.patentIDList = new ArrayList<String>();
		this.patentIDList.addAll(patentIDs);
	}
	
	//mathod
	private int getPatentNum(){
		return this.patentIDList.size();
	}
	
	public List<String> getIDList(){
		return this.patentIDList;
	}
	
	public String getIDByIndex(int index){
		return this.patentIDList.get(index);
	}
	
	private double[][] generateDissimMatrix(String dimeName){
		System.out.println("generate Dissim Matrix");
		int n = this.getPatentNum();
		double[][] dissimMatrix = new double[n][n];
		
		for(int i=0; i<n; i++){
			for(int j=i; j<n; j++){
				if(i == j) 
					dissimMatrix[i][j] = 0;
				else{
					Set<String> termSeti = this.patentGroup.
							getPatentByID(this.patentIDList.get(i)).getFeature(dimeName);
					Set<String> termSetj = this.patentGroup.
							getPatentByID(this.patentIDList.get(j)).getFeature(dimeName);
					double dissim = 1 - WordNetSim.getTermsSimilarity(termSeti, termSetj);
					dissimMatrix[i][j] = dissim;
					dissimMatrix[j][i] = dissim;
				}			
			}
		}
		
		return dissimMatrix;
	}
	
	private double[][] doMDS(double[][] dissimMatrix){
		System.out.println("MDS...");
		int n = this.getPatentNum();
		double[][] output = MDSJ.stressMinimization(dissimMatrix);	
		double[][] matrix = new double[n][output.length];
		
		for(int i=0; i<n; i++){
			for(int j=0; j<output.length; j++){
				System.out.print(output[j][i]+" ");
				matrix[i][j] = output[j][i];
			}
			System.out.println();
		}
		
		return matrix;
	}
	
	public double[][] getMatrixFromDissmatrix(String dimeName){
		double[][] dissimMatrix = this.generateDissimMatrix(dimeName);
		double[][] matrix = this.doMDS(dissimMatrix);
		
		return matrix;
	}
	
	//得到patent group的vector matrix
	public double[][] getPatentMatrix(String vecType, Set<String> featureSet){
		double[][] matrix = new double[this.getPatentNum()][featureSet.size()];
		int index = 0;
		
		for(String patentID : this.patentIDList){
			Patent patent = this.patentGroup.getPatentByID(patentID);
			matrix[index] = patent.getVector(vecType, this.patentGroup.getInvertedIndex(), featureSet);
			index++;
		}
		return matrix;
	}
	
	public double[][] getPatentEffectMatrix(String vecType, Set<String> featureSet){
		double[][] matrix = new double[this.getPatentNum()][featureSet.size()];
		int index = 0;
		
		for(String patentID : this.patentIDList){
			Patent patent = this.patentGroup.getPatentByID(patentID);
			matrix[index] = patent.getEffectVector(vecType, this.patentGroup.getInvertedIndex(), featureSet);
			index++;
		}
		return matrix;
	}
}
