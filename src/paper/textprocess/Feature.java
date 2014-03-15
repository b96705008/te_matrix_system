package paper.textprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import paper.clustering.cluster.ClusterGroup;
import paper.patent.Document;
import paper.patent.InvertedIndex;
import paper.techmatrix.TFMatrix;


public class Feature {

	public static void main(String[] args) {
		Document doc1 = new Document("d1");
		doc1.addTerm("t1", 1);
		doc1.addTerm("t2", 2);
		
		Document doc2 = new Document("d2");
		doc2.addTerm("t2", 1);
		doc2.addTerm("t3", 2);
		
		Document doc3 = new Document("d3");
		doc3.addTerm("t2", 1);
		doc3.addTerm("t4", 2);
		doc3.addTerm("t5", 2);
		
		ClusterGroup clusters = new ClusterGroup();
		clusters.addElement("c1", "d1");
		clusters.addElement("c1", "d2");
		clusters.addElement("c2", "d3");
		
		InvertedIndex inverted = new InvertedIndex();
		inverted.addDocument(doc1);
		inverted.addDocument(doc2);
		inverted.addDocument(doc3);
		inverted.indexing();
		
		Set<String> feats = new TreeSet<String>();
		feats.add("t1"); feats.add("t2"); feats.add("t3"); feats.add("t4"); feats.add("t5");
		
		//Feature.selectByDF(inverted);
		Feature.selectByCHI(inverted, feats, clusters, 0.0);
	
	}
	
	public static void sortFeatures(List<Feature> featureList, final boolean isDESC){
		//sort
		Collections.sort(featureList, new Comparator<Feature>(){
			@Override
			public int compare(Feature f0, Feature f1) {
				int flag = f0.getWeight().compareTo(f1.getWeight());
				if(isDESC) flag *= -1;
				return flag;
			}		
		});
	}
	
	public static void sortLabelFeatures(List<Feature> featureList){
		//sort
		Collections.sort(featureList, new Comparator<Feature>(){
			@Override
			public int compare(Feature f0, Feature f1) {
				int flag = 0;
				
				if(f0.getWeight() == f1.getWeight()){
					flag = f0.getDF().compareTo(f1.getDF()) * -1;
				}else{
					flag = f0.getWeight().compareTo(f1.getWeight()) * -1;
				}
				return flag;
			}		
		});
	}
	
	public static Set<String> selectByRank(List<Feature> featureList, int rank){
		Set<String> selectedFeats = new TreeSet<String>();
		
		//sort
		Feature.sortFeatures(featureList, true); //big to small
		double lastWeight = 0;
		for(int i=0; i<featureList.size(); i++){
			if(i < rank){
				selectedFeats.add(featureList.get(i).getName());
				lastWeight = featureList.get(i).getWeight();
				//featureList.get(i).show();
			}else if(i >= rank){
				if(featureList.get(i).getWeight() != lastWeight){
					break;
				}else{
					selectedFeats.add(featureList.get(i).getName());
					//featureList.get(i).show();
				}
			}
		}
		
		return selectedFeats;
		
	}
	
	public static Set<String> selectByDF(InvertedIndex inverted, Set<String> feats){
		//declare
		Set<String> selectedFeats = new TreeSet<String>();
		List<Feature> featureList = new ArrayList<Feature>();
		
		//construct featureList
		for(String term : feats){
			int df = inverted.getDF(term);
			featureList.add(new Feature(term, df));
		}
		
		//sort
		Feature.sortFeatures(featureList, false);
		
		//show
		for(Feature feature:featureList){
			//if(feature.getWeight() < 10)
				//selectedFeatureSet.add(feature.getName());
			feature.show();
		}	
		
		return selectedFeats;
	}
	
	public static Set<String> selectByTFIDF(InvertedIndex inverted, Set<String> feats, 
			double selectPercent){
		//declare
		Set<String> selectedFeats = new TreeSet<String>();
		List<Feature> featureList = new ArrayList<Feature>();
		
		//construct featureList
		for(String term : feats){
			int df = inverted.getDF(term);
			double tfidf = inverted.getTfIdf(term);
			featureList.add(new Feature(term, tfidf, df));
		}
		
		//sort big -> small
		Collections.sort(featureList, new Comparator<Feature>(){
			@Override
			public int compare(Feature f0, Feature f1) {
				int flag = 0;
				/*
				if(f0.getDF() == f1.getDF()){
					flag = f0.getWeight().compareTo(f1.getWeight()) * -1;
				}else{
					flag = f0.getDF().compareTo(f1.getDF()) * -1;
				}*/
				flag = f0.getWeight().compareTo(f1.getWeight()) * -1;
				return flag;
			}		
		});	
		
		//select	
		int selectSize = (int)(featureList.size() * selectPercent);
		if(selectSize > featureList.size()) 
			selectSize = featureList.size();
		
		int index = 0;
		for(Feature feature : featureList){
			if((++index) > selectSize) break;
			selectedFeats.add(feature.getName());
			feature.show();
		}	
		
		return selectedFeats;
	}
	
	public static Set<String> selectByCHI(InvertedIndex inverted, Set<String> feats, 
			ClusterGroup clusters, double ridPercent){
		//declare
		Set<String> selectedFeats = new TreeSet<String>();
		List<Feature> featureList = new ArrayList<Feature>();
		
		//construct featureList
		for(String term : feats){
			Set<String> postings = inverted.getPostingsByTerm(term);
			double avgChi = CHI.getAvgChiValue(postings, clusters);
			featureList.add(new Feature(term, avgChi));
		}
	
		//sort
		Feature.sortFeatures(featureList, false); //small to big
		
		//select	
		int ridFeatureSize = (int)(featureList.size() * ridPercent);
		if(ridFeatureSize > featureList.size()) 
			ridFeatureSize = featureList.size();
		
		boolean isFind = false;
		for(int i=0; i<featureList.size(); i++){
			if(i+1 <= ridFeatureSize) continue;
			
			if(!isFind && i != 0){
				double curScore = featureList.get(i).getWeight();
				double prevScore = featureList.get(i-1).getWeight();
				if(curScore != prevScore) isFind = true;
			}
			
			if(isFind){
				Feature feature = featureList.get(i);
				selectedFeats.add(feature.getName());
				feature.show();
			}
		}
	
		return selectedFeats;
	}
	
	//°µcluster labeling!!!
	public static void labelCluster(InvertedIndex inverted, Set<String> feats,
			ClusterGroup clusters, int rank, boolean isTfidf){
		
		//get CHI value for every term for every cluster 
		for(String term : feats){
			int df = inverted.getDF(term);
			if(df==0){
				System.out.println(term + " disappear!");
				continue;
			}
			
			double tfidf = inverted.getTfIdf(term);
			Set<String> postings = inverted.getPostingsByTerm(term);
			Map<String, CHI> chiMap = CHI.getChiMap(postings, clusters);
			
			for(String clusterID : chiMap.keySet()){
				double chiValue = chiMap.get(clusterID).getChiValue();
				double weight = chiValue;
				if(isTfidf) chiValue = chiValue * tfidf;
				clusters.addLabelFeature(clusterID, new Feature(term, weight, df));
			}
		}
		
		clusters.sortLabelByWeight();
		clusters.showLabel(rank);
	}
	
	//property
	private String name;
	private double weight;
	private int df;
	
	public Feature(String name){
		this.name = name;
		this.weight = 0;
	}
	
	public Feature(String name, double weight){
		this.name = name;
		this.weight = weight;
	}
	
	public Feature(String name, double weight, int df){
		this(name, weight);
		this.df = df;
	}
	
	public String getName(){
		return this.name;
	}
	
	public Double getWeight(){
		return this.weight;
	}
	
	public Double getDF(){
		return (double)this.df;
	}
	
	public void addWeight(double weight){
		this.weight += weight;
	}
	
	public void show(){
		System.out.println(this.name+" "+this.weight+" "+this.df);
	}
}
