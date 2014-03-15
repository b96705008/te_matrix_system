package paper.textprocess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import paper.Util;
import paper.clustering.cluster.ClusterGroup;
import paper.techmatrix.TFMatrix;

public class CHI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static Map<String, CHI> getChiMap(Set<String> postings, 
			ClusterGroup clusters){
		Map<String, CHI> chiMap = new TreeMap<String, CHI>();
		
		//for every cluster, new CHI and set t_c, nt_c
		for(String clusterID : clusters.getClusterIDs()){
			Set<String> clusterSet = clusters.getClusterByID(clusterID);
			CHI chi = new CHI(clusterSet.size());
			int t_c = Util.getIntersectSet(postings, clusterSet).size();
			int nt_c = clusterSet.size() - t_c;
			chi.setX_C(t_c, nt_c);
			//chiList.add(chi);
			chiMap.put(clusterID, chi);
		}
		
		//add t_nc, nt_nc
		for(String clusterID : chiMap.keySet()){
			CHI chi = chiMap.get(clusterID);
			
			for(String otherClusterID : chiMap.keySet()){
				if(otherClusterID.equals(clusterID)) continue;
				CHI other_chi = chiMap.get(otherClusterID);	
				chi.addX_NC(other_chi.getT_C(), other_chi.getNT_C());
			}
		}
		
		return chiMap;
	}
	
	public static double getAvgChiValue(Set<String> termInPatentSet, 
			ClusterGroup clusters){
		
		double avgChiValue = 0;
		int docNum = clusters.getElemSize();
		Map<String, CHI> chiMap = getChiMap(termInPatentSet, clusters);
		
		//get average
		for(String clusterID : chiMap.keySet()){
			CHI chi = chiMap.get(clusterID);
			double clusterProb = chi.getSize() / docNum;
			avgChiValue += clusterProb * chi.getChiValue();
		}
		
		return avgChiValue;
	}
	
	
	//property
	private int t_c; //A
	private int t_nc; //B
	private int nt_c; //C
	private int nt_nc; //D
	private int size; //docNum belong to this class
	
	//constructor
	public CHI(){
		this.t_c = 0;
		this.t_nc = 0;
		this.nt_c = 0;
		this.nt_nc = 0;
	}
	
	public CHI(int size){
		this();
		this.size = size;
	}
	
	public void show(){
		String result = "";
		result += "t_c: " + this.t_c;
		result += " t_nc: " + this.t_nc;
		result += " nt_c: " + this.nt_c;
		result += " nt_nc: " + this.nt_nc;
		System.out.println(result);
	}
	
	public void setX_C(int t_c, int nt_c){
		this.t_c = t_c;
		this.nt_c = nt_c;
	}
	
	public void addX_NC(int other_t_c, int other_nt_c){
		this.t_nc += other_t_c;
		this.nt_nc += other_nt_c;
	}
	
	public int getT_C(){
		return this.t_c;
	}
	
	public int getNT_C(){
		return this.nt_c;
	}
	
	public double getSize(){
		return this.size;
	}
	
	private int getN(){
		int n = this.t_c + this.t_nc + this.nt_c + this.nt_nc;
		return n;
	}
	
	public double getChiValue(){
		int n = this.getN();
		if(n == 0) 
			return 0;
		double denominator = (t_c+nt_c) + (t_nc+nt_nc) + (t_c+t_nc) + (nt_c+nt_nc);
		double moleItem = (t_c*nt_nc - t_nc*nt_c); //(AD-BC)
		double molecular = n*moleItem*moleItem;
		double chiValue = molecular/denominator;
	
		return chiValue;
	}
}
