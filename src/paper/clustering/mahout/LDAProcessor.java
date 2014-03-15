package paper.clustering.mahout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import org.apache.mahout.driver.MahoutDriver;
import org.apache.mahout.math.DenseVector;

import paper.Config;
import paper.clustering.cluster.ClusterGroup;
import paper.clustering.cluster.ClusterProcessor;
import paper.patent.PatentGroup;
import paper.textprocess.Feature;

public class LDAProcessor {

	public static void main(String[] args) throws Throwable {
		SequenceIO.readLdaTopicOut(Config.topicOutFile);
	}
	
	//property
	private PatentGroup patentGroup;
	private List<String> patentIDList;
	
	//constructor
	public LDAProcessor(PatentGroup patentGroup){
		this.patentGroup = patentGroup;
		this.patentIDList = patentGroup.getPatentIDList();
	}
	
	public LDAProcessor(PatentGroup patentGroup, TreeSet<String> patentIDs){
		this.patentGroup = patentGroup;
		this.patentIDList = new ArrayList<String>();
		this.patentIDList.addAll(patentIDs);
	}
	
	//method: dimeName(tech or effect), topicNum = 5,10...., maxItr=越多越收斂 (實驗以200round為準)
	public void doLDA(String dimeName, int topicNum, int maxItr) throws Throwable{
		//generate 1.TF vectors 2.Dictionary
		SequenceIO.generateVectorFile("tf", dimeName, this.patentGroup, this.patentIDList);
		SequenceIO.generateDictionary(dimeName, this.patentGroup);
		
		//transform vectors to matrix file
		String rowidJob = "rowid";
		//rowidJob += " -i " + Config.getVectorDir("tf", dimeName);
		rowidJob += " -i " + Config.vectorDir;
		rowidJob += " -o " + Config.matrixDir;
		MahoutDriver.main(rowidJob.split(" "));
		
		//run Mahout CvbLocalJob
		String cvbLocalJob = "cvb0_local";
		cvbLocalJob += " -i " + Config.matrixFile;
		cvbLocalJob += " -d " + Config.dictFile;
		cvbLocalJob += " -top " + topicNum;
		cvbLocalJob += " -do " + Config.docOutFile;
		cvbLocalJob += " -to " + Config.topicOutFile;
		cvbLocalJob += " -m " + maxItr;
		cvbLocalJob += " -ntt 2";
		cvbLocalJob += " -nut 2";
		//cvbLocalJob += " -a " + alpha;
		MahoutDriver.main(cvbLocalJob.split(" "));
	}
	
	public Map<String, DenseVector> getDocTopicVecs(){
		Map<String, DenseVector> docToTopics = null;
		try {
			docToTopics = SequenceIO.readLdaDocOut(Config.docOutFile, 
					Config.docIndexFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return docToTopics;
	}
	
	private ClusterGroup getTopicClusters(String dimeName, int rankThold, 
			double weightThold) throws IOException{
		//declare
		ClusterProcessor cProcessor = new ClusterProcessor();
		Map<String, DenseVector> docToTopics = SequenceIO.readLdaDocOut(
				Config.docOutFile, Config.docIndexFile);
		
		for(String patentID : docToTopics.keySet()){
			DenseVector topicVec = docToTopics.get(patentID);
			for(int clusterID = 0; clusterID < topicVec.size(); clusterID++){
				double weight = topicVec.get(clusterID);
				cProcessor.addClusterElem(patentID, weight, clusterID+"");
			}
		}
			
		return cProcessor.getClusters(rankThold, weightThold);
	}
	
	private void labelClusterByLda(String dimeName, ClusterGroup clusters) throws IOException{
		Map<String, DenseVector> topicToTerms = 
				SequenceIO.readLdaTopicOut(Config.topicOutFile);
		Set<String> terms = this.patentGroup.getFeatureSet(dimeName);
		
		for(String clusterID : topicToTerms.keySet()){	
			DenseVector termVec = topicToTerms.get(clusterID);
			int index = 0;
			
			for(String term : terms){			
				double probWeight = termVec.get(index);
				index++;
				int df = this.patentGroup.getInvertedIndex().getDF(term);				
				clusters.addLabelFeature(clusterID, new Feature(term, probWeight, df));
			}
		}
		clusters.sortLabelByWeight();
	}
	
	public ClusterGroup getClustersFromLda(String dimeName, int topicNum, int maxItr, 
			 int rankThold, double weightThold){
		ClusterGroup topicClusters = null;
		
		//1.do LDA  2.get clusters from docToTopics  3.label cluster by topicToTerms 
		try {
			this.doLDA(dimeName, topicNum, maxItr);
			topicClusters = this.getTopicClusters(dimeName, rankThold, weightThold);
			this.labelClusterByLda(dimeName, topicClusters);
			topicClusters.showLabel(15);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return topicClusters;
	}
}
