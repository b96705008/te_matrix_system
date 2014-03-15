package paper.techmatrix;

import java.util.*;

import paper.Util;
import paper.clustering.cluster.ClusterGroup;

public class Evaluator {
	public static void main(String[] args){
		ClusterGroup aclusters = new ClusterGroup();
		aclusters.addElement("c1", "d1");
		aclusters.addElement("c1", "d2");
		aclusters.addElement("c2", "d3");
		aclusters.addElement("c2", "d4");
		aclusters.addElement("c3", "d5");
		
		ClusterGroup tclusters = new ClusterGroup();
		tclusters.addElement("c1", "d1");
		tclusters.addElement("c2", "d2");
		tclusters.addElement("c2", "d3");
		tclusters.addElement("c2", "d4");
		tclusters.addElement("c3", "d5");
		
		evaluateByPurity(aclusters, tclusters);
		evaluateByPR(aclusters, tclusters);
	}
	
	private static Set<String> getClusterPair(Map<String, Set<String>> evaluateMap){
		
		Set<String> clusterPair = new TreeSet<String>();
		Iterator<String> kItr = evaluateMap.keySet().iterator();	
		while(kItr.hasNext()){
			String clusterNum = kItr.next();
			Set<String> patentSet = evaluateMap.get(clusterNum);
			if(patentSet.size() <= 1)
				continue;
			
			Object[] patentArr = patentSet.toArray();
			for(int i=0; i<patentArr.length; i++){
				for(int j=i+1; j<patentArr.length; j++){
					String pair = (String)patentArr[i]+"_"+(String)patentArr[j];
					clusterPair.add(pair);
				}
			}
			
		}
		return clusterPair;
	}
	
	public static void evaluateByPR(ClusterGroup answerClusters, ClusterGroup testingClusters){
		Map<String, Set<String>> answerMap = answerClusters.getClusters();
		Map<String, Set<String>> testingMap = testingClusters.getClusters();
		
		double recall = 0;
		double precision = 0;
		double f1 = 0;
		Set<String> answerPairs = getClusterPair(answerMap);
		Set<String> testingPairs = getClusterPair(testingMap);
		int tp = 0;
		
		for(String testingPair : testingPairs){
			if(answerPairs.contains(testingPair)){
				tp++;
			}
		}
		
		precision = (double)tp/testingPairs.size();
		recall = (double)tp/answerPairs.size();
		f1 = 2 * precision * recall / (precision + recall);
		
		System.out.println("recall: "+recall+" precision: "+precision);
		System.out.println("f1: "+f1);
	}
	
	public static void evaluateByPurity(ClusterGroup answerClusters, ClusterGroup testingClusters){
		int elemSize = testingClusters.getElemSize();
		Map<String, Set<String>> answerMap = answerClusters.getClusters();
		Map<String, Set<String>> testingMap = testingClusters.getClusters();
		
		int tp = 0;
		for(String testingClusterID : testingMap.keySet()){
			int majorityNum = 0;
			
			for(String answerClusterID : answerMap.keySet()){
				int classIntersectNum = Util.getIntersectSet(testingMap.get(testingClusterID), 
						answerMap.get(answerClusterID)).size();
				majorityNum = Math.max(majorityNum, classIntersectNum);
			}
			tp += majorityNum;
		}
		
		double purity = (double)tp / elemSize;
		
		System.out.println("purity: "+ purity);
	}
	
	public static void evaluateByNMI(ClusterGroup answerClusters, ClusterGroup testingClusters){
		
	}
}
