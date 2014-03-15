package paper.clustering.weka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import paper.clustering.cluster.ClusterGroup;
import paper.patent.Document;
import paper.patent.InvertedIndex;
import paper.patent.Patent;
import paper.patent.PatentGroup;
import paper.techmatrix.TFMatrix;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.PrincipalComponents;

public class PatentCluster {

	public static void main(String[] args) {
		
	}
	
	private Instances clusterSet;
	private PatentGroup patentGroup;
	
	public PatentCluster(String vecType, String dimeName, PatentGroup patentGroup){
		this.patentGroup = patentGroup;
		this.setVectorFormat(dimeName);	
		this.addClusterData(vecType, dimeName);
		//this.doPca();
	}
	
	private void setVectorFormat(String dimeName){
		System.out.println("setVector...");
		Set<String> termSet = this.patentGroup.getFeatureSet(dimeName);
		
		//Declare numeric attributes and the feature vector
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for(String term:termSet){
			Attribute attr = new Attribute(term);
			attributes.add(attr);
		}
		
		//Declare the Instances
		this.clusterSet = new Instances("ClusterSet", attributes, 0);
	}
	
	private void addClusterData(String vecType, String dimeName){
		System.out.println("addClusterData...");
		Set<String> feats = this.patentGroup.getFeatureSet(dimeName);
		InvertedIndex inverted = this.patentGroup.getInvertedIndex();
		Map<String, Document> docMap = inverted.getDocMap();
		
		//add data
		for(String patentID : docMap.keySet()){
			Patent patent = (Patent)docMap.get(patentID);
			double[] values = patent.getVector(vecType, inverted, feats);
			Instance inst = new DenseInstance(1.0, values);
			this.clusterSet.add(inst);
		}
	}
	
	public ClusterGroup clusteringData(int num){
		ClusterGroup clusters = new ClusterGroup();
		try {
			System.out.println("clusterData...");
			List<String> patentIDList = this.patentGroup.getPatentIDList();
			
			String[] options = weka.core.Utils.splitOptions("-N "+num);
			SimpleKMeans clusterer = new SimpleKMeans();
			clusterer.setOptions(options);     // set the options
			clusterer.buildClusterer(this.clusterSet);    // build the clusterer
		
			/*
			String[] options = weka.core.Utils.splitOptions("-I 100 -N 3");
			EM clusterer = new EM(); // new instance of clusterer 
			clusterer.setOptions(options); // set the options 
			clusterer.buildClusterer(clusterSet); // build the clusterer
			*/
			
			System.out.println("# - cluster - distribution"); 
			for (int i = 0; i < this.clusterSet.numInstances(); i++) {
				int clusterID = clusterer.clusterInstance(this.clusterSet.instance(i));
				double[] dist = clusterer.distributionForInstance(this.clusterSet.instance(i)); 
				String patentID = patentIDList.get(i);
						
				System.out.print(patentID);
				System.out.print(" - ");
				System.out.print(clusterID);
				//System.out.print(" - ");
				//System.out.print(Utils.arrayToString(dist));
				System.out.println();
				
				clusters.addElement(clusterID+"", patentID);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return clusters;
	}
	
	public void doPca(){
		System.out.println("doPca...");
		try {
			String[] options = weka.core.Utils.splitOptions("-A -1 -R 0.95");
			PrincipalComponents pca = new PrincipalComponents();
			pca.setOptions(options);
			pca.setInputFormat(this.clusterSet);
			//pca.setMaximumAttributes(5);
			this.clusterSet = Filter.useFilter(this.clusterSet, pca); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Attribute num: "+this.clusterSet.numAttributes());
	}
	
	public void normalizeData(){
		System.out.println("normalizeData...");
		
		try {
			Normalize filter = new Normalize();
			filter.setInputFormat(this.clusterSet);
			this.clusterSet = Filter.useFilter(this.clusterSet, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
