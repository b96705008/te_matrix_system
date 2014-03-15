package paper.clustering.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.NamedVector;

public class ClusterValidity {

	public static void main(String[] args) {
		ClusterGroup clusters = new ClusterGroup();
		clusters.addElement("c1", "p1");
		clusters.addElement("c1", "p2");
		clusters.addElement("c2", "p3");
		clusters.addElement("c2", "p4");
		
	}
	
	//property
	private ClusterGroup clusters; //ClusterGroup物件
	private Map<String, NamedVector> vectorMap; //patentID - mahout namedVector 對照表
	private DistanceMeasure measure; //使用的距離measure, 本論文用CosineDistanceMeasure
	
	public ClusterValidity(Map<String, NamedVector> vectorMap){
		this.vectorMap = vectorMap;
		this.measure = new CosineDistanceMeasure();
		
	}
	
	public ClusterValidity(ClusterGroup clusters, Map<String, NamedVector> vectorMap){
		this(vectorMap);
		this.clusters = clusters;
	}
	
	public void setClusters(ClusterGroup clusters){
		this.clusters = clusters;
	}
	
	public void setDistanceMeasure(DistanceMeasure measure){
		this.measure = measure;
	}
	
	public void setVectorMap(Map<String, NamedVector> vectorMap){
		this.vectorMap = vectorMap;
	}
	
	//get complement of cluster i
	private List<String> getComplementCluster(String focusdClusterID){
		List<String> compCluster = new ArrayList<String>();
		
		for(String clusterID : this.clusters.getClusterIDs()){
			if(clusterID.equals(focusdClusterID)) continue;
			Set<String> cluster = this.clusters.getClusterByID(clusterID);
			compCluster.addAll(cluster);
		}
		
		return compCluster;
	}
	
	//inter cluster distance
	private double interPairwiseSim(Collection<String> cluster1, Collection<String> cluster2){
		double pwSim = 0;
		int pairNum = 0; 
		
		for(String objID1 : cluster1){
			for(String objID2 : cluster2){
				pwSim += 1 - this.measure.distance(this.vectorMap.get(objID1), 
						this.vectorMap.get(objID2));
				pairNum++;
			}
		}
		
		if(pairNum != 0) return pwSim / pairNum;
		else return 0;
	}
	
	//intra cluster distance
	private double intraPairwiseSim(Set<String> cluster){
	
		double pwSim = 0;
		int pairNum = 0;
		List<String> objectIDs = new ArrayList<String>();
		objectIDs.addAll(cluster);
		
		for(int i=0; i<objectIDs.size(); i++){
			for(int j=i+1; j<objectIDs.size(); j++){
				pwSim += 1 - this.measure.distance(this.vectorMap.get(objectIDs.get(i)), 
						this.vectorMap.get(objectIDs.get(j)));
				pairNum++;
			}
		}
		
		if(pairNum != 0) return pwSim / pairNum;
		else return 0;
	}
	
	//min-max
	public double getMinMax(){
		double min_max = 0;
		
		for(String clusterID : this.clusters.getClusterIDs()){
			double localMinMax = 0;	
			Set<String> cluster = this.clusters.getClusterByID(clusterID);
			List<String> compCluster = this.getComplementCluster(clusterID);
			//caculate localMinMax
			localMinMax += (this.interPairwiseSim(cluster, compCluster) + 1);
			localMinMax /= (this.intraPairwiseSim(cluster) + 1);
			min_max +=  localMinMax;
		}
		
		if(this.clusters.getClustersSize() != 0)
			min_max /= this.clusters.getClustersSize();
			
		return min_max;
	}
	
	//gamma function
	private  double gammaFun(){
		double result = 0;
		int alpha = 3;
		int alphaFact = 3*2*1;
		double beta = Math.sqrt(this.clusters.getElemSize()) / 2;
		int x = this.clusters.getClustersSize();
		
		double denominator = alphaFact * Math.pow(beta, alpha);
		double molecular = Math.pow(x, alpha-1);
		molecular *= Math.pow(Math.E, -1*x/beta);
		result = molecular / denominator;
		
		//System.out.println(result);
		return result;
	}
	
	//min-max adjusted by Cluster Number Preference
	public double getMinMaxCNP(){
		return this.getMinMax() / this.gammaFun();
	}
	
	//Davies-Bouldin Index
	public double getDBIndex(){
		double dbIndex = 0;
		Set<String> clusterIDs = this.clusters.getClusterIDs();
		
		for(String clusterID : clusterIDs){
			Set<String> cluster = this.clusters.getClusterByID(clusterID);
			double localMaxDbIndex = 0;

			for(String otherClusterID : clusterIDs){
				if(clusterID.equals(otherClusterID)) continue;
				Set<String> otherCluster = this.clusters.getClusterByID(otherClusterID);
				double localDbIndex = ( this.interPairwiseSim(cluster, otherCluster) + 1) 
									/ (this.intraPairwiseSim(cluster) + this.intraPairwiseSim(otherCluster) + 1); 
				localMaxDbIndex = Math.max(localMaxDbIndex, localDbIndex);
			}
			
			dbIndex += localMaxDbIndex;
		}
		
		int c = this.clusters.getClustersSize();
		if(c > 0) dbIndex /= c;
		
		return dbIndex;
	}
	
	public double getCVByType(String type){
		if(type.equals("MinMax"))
			return this.getMinMax();
		else if(type.equals("MinMaxCNP"))
			return this.getMinMaxCNP();
		else if(type.equals("DBIndex"))
			return this.getDBIndex();
		else
			return 0;
	}
	
	public void showValidity(){
		System.out.println("MinMax: " + this.getMinMax());
		System.out.println("MinMax CNP: " + this.getMinMaxCNP());
		System.out.println("DB Index: " + this.getDBIndex());
	}
}
