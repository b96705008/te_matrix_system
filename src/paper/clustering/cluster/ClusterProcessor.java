package paper.clustering.cluster;

import java.util.*;

import paper.techmatrix.TFMatrix;

//論文3.2.4, 用來對FCM分群完的權重，來篩選出最後的分群結果
public class ClusterProcessor {

	public static void main(String[] args) {
	}
	
	public static void sortElemList(List<ClusterElem> elemList){
		//sort
		Collections.sort(elemList, new Comparator<ClusterElem>(){
			@Override
			public int compare(ClusterElem e0, ClusterElem e1) {
				int flag = e0.getWeight().compareTo(e1.getWeight());
				return flag*-1;
			}		
		});
	}
	
	private static boolean isUpperBound(int rank, double weight){
		double upperBound = 1.0;
		for(int i=0; i<rank; i++)
			upperBound = upperBound / 2.0;
		if(weight >= upperBound)
			return true;
		else 
			return false;
	}
	
	
	private class ClusterElem{
		private double weight;
		private String clusterID;
		
		public ClusterElem(double weight, String clusterID){
			this.weight = weight;
			this.clusterID = clusterID;
		}
		
		public Double getWeight() {
			return weight;
		}
		
		public String getClusterID() {
			return clusterID;
		}
		
	}
	
	private Map<String, List<ClusterElem>> clusterMap;
	
	public ClusterProcessor(){
		this.clusterMap = new TreeMap<String, List<ClusterElem>>();
	}
	
	public void addClusterElem(String patentID, double weight, String clusterID){
		List<ClusterElem> elemList = null;
		ClusterElem elem = new ClusterElem(weight, clusterID);
		//get elemList instance
		if(this.clusterMap.containsKey(patentID))
			elemList = this.clusterMap.get(patentID);
		else
			elemList = new ArrayList<ClusterElem>();
		//update clusterMap
		elemList.add(elem);
		this.clusterMap.put(patentID, elemList);
	}
	
	//篩選的method, 回傳ClusterGroup物件
	public ClusterGroup getClusters(int rankThold, double weightThold){
		if(rankThold <= 0) rankThold = 1;
		ClusterGroup clusters = new ClusterGroup();
		
		for(String patentID : this.clusterMap.keySet()){
			List<ClusterElem> elemList = this.clusterMap.get(patentID);
			sortElemList(elemList); //sort by weight
			int rank = 0;
			
			for(ClusterElem elem : elemList){
				rank++;
				
				if(elem.getWeight() > 0.5 || rank == 1){
					clusters.addElement(elem.getClusterID(), patentID);
					//System.out.println(patentID+"\t"+elem.getWeight()+" -> cluster: "+elem.getClusterID());
					if(elem.getWeight() > 0.5)	break;
				}else if(rank <= rankThold && elem.getWeight() > weightThold){
					clusters.addElement(elem.getClusterID(), patentID);
					//System.out.println(patentID+"\t"+elem.getWeight()+" -> cluster: "+elem.getClusterID());
				}else{
					break;
				}
				
			}//end for	
		}//end for 
		
		return clusters;
	}
}
