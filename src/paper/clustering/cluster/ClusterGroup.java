package paper.clustering.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import paper.patent.Patent;
import paper.textprocess.Feature;

//用來操作一群Cluster的類別
public class ClusterGroup implements Iterable<Set<String>>{

	public static void main(String[] args) {
	}
	
	//===property
	private int size;
	private Map<String, Set<String>> clusters; //clusterID-patentID set 對照表
	private Map<String, List<Feature>> labelSet; //clusterID-labeling term set 對照表
	
	//===constructor
	public ClusterGroup(){
		this.size = 0;
		this.clusters = new TreeMap<String, Set<String>>();
		this.labelSet = new TreeMap<String, List<Feature>>();
	}
	
	//===method
	//getter
	public Map<String, Set<String>> getClusters(){
		return this.clusters;
	}
	
	public Set<String> getClusterByID(String clusterID){
		return this.clusters.get(clusterID);
	}
	
	public Set<String> getClusterIDs(){
		return this.clusters.keySet();
	}
	
	public int getClustersSize(){
		return this.clusters.size();
	}
	
	public int getElemSize(){
		return this.size;
	}
	
	//setter
	public void addCluster(String clusterID, Set<String> patentIDs){
		for(String patentID : patentIDs){
			this.addElement(clusterID, patentID);
		}
	}
	
	public void addElement(String clusterID, String patentID){
		Set<String> patentSet = null;
		
		if(this.clusters.containsKey(clusterID)){
			patentSet = this.clusters.get(clusterID);
		}else{
			patentSet = new TreeSet<String>();
		}
		patentSet.add(patentID);
		this.clusters.put(clusterID, patentSet);
		this.size++;
	}
	
	//===處理各cluster的labeling===
	
	public void addLabelFeature(String clusterID, Feature feature){
		List<Feature> featureList = null;
		
		if(this.labelSet.containsKey(clusterID)){
			featureList = this.labelSet.get(clusterID);
		}else{
			featureList = new ArrayList<Feature>();
		}
		featureList.add(feature);
		this.labelSet.put(clusterID, featureList);
	}
	
	public void sortLabelByWeight(){
		for(List<Feature> featureList : labelSet.values()){
			//Feature.sortFeatures(featureList, true);
			Feature.sortLabelFeatures(featureList);
		}
	}
	
	public void showLabel(int rankTh){
		for(String clusterID : this.labelSet.keySet()){
			System.out.println("clusterID - "+clusterID+": ");
			List<Feature> featureList = this.labelSet.get(clusterID);
			
			//select by rank
			int curRank = 0;
			double lastScore = Double.MIN_VALUE;
			double lastDF = Double.MIN_VALUE;
			
			for(Feature feature : featureList){
				curRank++;
				double curScore = feature.getWeight();
				double curDF = feature.getDF();
				
				if(curRank > rankTh && curScore != lastScore
						&& curDF != lastDF) break;
				feature.show();
				lastScore = curScore;
			}
			
		}
	}

	@Override
	public Iterator<Set<String>> iterator() {
		return new Iterator<Set<String>> (){	 
			Iterator<String> idItr = getClusterIDs().iterator();
			
			@Override
			public boolean hasNext() {
				return idItr.hasNext();
			}

            @Override
            public Set<String> next() {
            	String clusterID = idItr.next();
            	Set<String> patentIDs = getClusterByID(clusterID);
            	return patentIDs;
            }
            
            @Override
            public void remove() {
            	throw new UnsupportedOperationException();
            }              
		 };
	}
}
