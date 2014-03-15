package paper.techmatrix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;

import paper.backend.Database;
import paper.clustering.cluster.ClusterGroup;
import paper.clustering.cluster.ClusterValidity;
import paper.clustering.mahout.LDAProcessor;
import paper.clustering.mahout.MahoutCluster;
import paper.clustering.mahout.SequenceIO;
import paper.clustering.weka.WekaCluster;
import paper.patent.*;
import paper.semantic.PatentMapping;
import paper.semantic.TermMapping;
import paper.semantic.matrix.JamaMath;
import paper.semantic.matrix.LSI;
import paper.textprocess.Feature;

public class TFMatrix {
	
	public static void main(String[] args) throws Throwable {
		Database db =  Database.getInstance();
		
		//===select Field===
		TFMatrix matrix = null;
		matrix = new TFMatrix("DDS", 2, db);
		//matrix = new TFMatrix("LEDS", 2, db);
		//matrix = new TFMatrix("QDGP", 2, db);
		//matrix = new TFMatrix("SRD", 1, db);
		//matrix = new TFMatrix("CN", 1, db);
		
		//===feature select===
		//matrix.featureSelection("tech", 0.8);
		
		
		//===tech clustering===
		
		//matrix.useTfidfClustering("tech", 2);
		//matrix.useWnsTfidfClustering("tech", 2);
		matrix.useLSITfidfClustering("tech", 0.6, 2);
		//matrix.useLSITfidfClustering("tech", 0.4, 2);
		//matrix.useLSITfidfClustering("tech", 0.8, 2);
		//matrix.useLDATfidfClustering("tech", 20, 100, 2);
		
		//===effect clustering===
		//matrix.useTfidfClustering("effect", 2);
		//matrix.useEffectTfidfClustering(2);
		//matrix.useWnsTfidfClustering("effect", 2);
		//matrix.useLSITfidfClustering("effect", 0.6, 2);
		//matrix.useLDATfidfClustering("effect", 5, 200, 2);
		
		//matrix.constructByLDA("tech", 3, 500, 50.0/3, 1);
		//matrix.constructByLDA("effect", 5, 1000, 50.0/3, 2);
		//matrix.constructByFeature("func", 10, 9, 2, 1.0/9);
		//matrix.constructByFeature("tech", 10, 4, 2);
		System.exit(0);
	}
	
	private String field;
	private Map<String, ClusterGroup> clustersMap;
	private PatentGroup patentGroup; //patents for test 
	
	public TFMatrix(String field, int level, Database db){
		this.field = field;
		this.initClustersMap();
		Set<String> patentIDs = db.processTFData(field, this.clustersMap, level);
		this.patentGroup = new PatentGroup(patentIDs);
	}
	
	//====process real data====
	private void initClustersMap(){
		//String[] dimeArr = {"tech", "effect", "use", "func"};
		String[] dimeArr = {"tech", "effect"};
		this.clustersMap = new HashMap<String, ClusterGroup>();
		
		for(String dime : dimeArr){
			this.clustersMap.put(dime, new ClusterGroup());
		}
	}
	
	public ClusterGroup getTFClusters(String dimeName){
		if(this.clustersMap.containsKey(dimeName))
			return this.clustersMap.get(dimeName);
		else
			return null;
	}
	
	public Set<String> getIDsFromClusters(String dimeName){
		Set<String> patentIDs = new TreeSet<String>();
		ClusterGroup clusters = this.getTFClusters(dimeName);
		
		for(Set<String> cluster : clusters){
			patentIDs.addAll(cluster);
		}
		return patentIDs;
	}
	
	public void setClustersMap(String field, int level){
		Database db =  Database.getInstance();
		this.initClustersMap();
		db.processTFData(field, this.clustersMap, level);
	}
	
	//====feature select===
	public void featureSelection(String dimeName, double selectPercent){
		Set<String> selectedFeats = Feature.selectByTFIDF(this.patentGroup.getInvertedIndex(), 
				this.patentGroup.getFeatureSet(dimeName), selectPercent);
		this.patentGroup.setFeatures(dimeName, selectedFeats);
	}
	
	//====clustering testing data====
	public void constructByLDA(String dimeName, int k, int maxItr, int rankTh){
		LDAProcessor lda = new LDAProcessor(this.patentGroup);
		ClusterGroup topicClusters = lda.getClustersFromLda(dimeName, k, maxItr, rankTh, 1.0/k);
		Evaluator.evaluateByPR(this.getTFClusters(dimeName), topicClusters);
	}
	
	//1. prepare data  2. construct by clustering
	
	private ClusterValidity prepareData(String vecType, String dimeName){
		ClusterValidity cv = null;
		try {
			SequenceIO.generateVectorFile(vecType, dimeName, this.patentGroup, this.getIDsFromClusters(dimeName));
			Map<String, NamedVector> vectorMap = SequenceIO.readVector();
			cv = new ClusterValidity(vectorMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return cv;
	}
	
	private ClusterValidity prepareData(double[][] matrix, List<String> idList){
		ClusterValidity cv = null;
		try {
			SequenceIO.generateVectorFile(matrix, idList);
			Map<String, NamedVector> vectorMap = SequenceIO.readVector();
			cv = new ClusterValidity(vectorMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return cv;
	}
	
	private ClusterValidity prepareData(Map<String, DenseVector> denVecMap){
		ClusterValidity cv = null;
		try {
			SequenceIO.generateVectorFile(denVecMap);
			Map<String, NamedVector> vectorMap = SequenceIO.readVector();
			cv = new ClusterValidity(vectorMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return cv;
	}
	
	public void labelClusterByDime(String dimeName, ClusterGroup testClusters){
		if(dimeName.equals("tech"))
			Feature.labelCluster(this.patentGroup.getInvertedIndex(), 
				this.patentGroup.getFeatureSet(dimeName), testClusters, 15, true);
		else
			Feature.labelCluster(this.patentGroup.getInvertedIndex(), 
					this.patentGroup.getFilteredEffectTerms(), testClusters, 15, false);
	}
	
	public ClusterGroup constructMatrix(String dimeName, int k, int rankTh) {
		ClusterGroup testClusters = null;
		try {
			testClusters = MahoutCluster.clusterData(k, rankTh, 1.0/k);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Evaluator.evaluateByPR(this.getTFClusters(dimeName), testClusters);
		Evaluator.evaluateByPurity(this.getTFClusters(dimeName), testClusters);
		this.labelClusterByDime(dimeName, testClusters);
		return testClusters;
	}
	
	//iterative clustering testing
	
	//===feature selection clustering===
	public void constructByFeature(String dimeName, int round, int k, int rankTh) throws Exception{
		ClusterValidity cv = this.prepareData("tfidf", dimeName);
		Set<String> feats = this.patentGroup.getFeatureSet(dimeName);
		
		for(int i=0; i<round; i++){
			System.out.println("feature size: "+this.patentGroup.getFeatureSize(dimeName));
			ClusterGroup testClusters = this.constructMatrix(dimeName, k, rankTh);
			cv.setClusters(testClusters);
			System.out.println("MinMax: " + cv.getMinMax() + " MinMax CNP: " + cv.getMinMaxCNP());
			
			if(i != (round-1)) {
				feats = Feature.selectByCHI(this.patentGroup.getInvertedIndex(), feats, testClusters, 0.1);
				this.patentGroup.setFeatures(dimeName, feats);
				cv = this.prepareData("tfidf", dimeName);
			}	
		}
	}
	
	//===TfIdf clustering===
	public void useTfidfClustering(String dimeName, int rankTh) throws Exception{
		System.out.println("use TfIdf Clustering.....");
		int startCNum = 1;
		int endCNum = 10;
		//int startCNum = 7;
		//int endCNum = 7;
		ClusterValidity cv = this.prepareData("tfidf", dimeName);
		
		for(int i=startCNum; i<=endCNum; i++){
			System.out.println("Cluster Num: "+i);
			ClusterGroup testClusters = this.constructMatrix(dimeName, i, rankTh);
			cv.setClusters(testClusters);
			cv.showValidity();
		}
	}
	
	//===Pure WordNet clustering===
	public void semanticClustering(String dimeName){
		System.out.println("use Pure WordNet Clustering.....");
		
		int startCNum = 1;
		int endCNum = 10;
		TreeSet<String> patentIDs =(TreeSet<String>)this.getIDsFromClusters(dimeName);
		PatentMapping pm = new PatentMapping(this.patentGroup, patentIDs);
		double[][] matrix = pm.getMatrixFromDissmatrix(dimeName);
		List<String> idList = pm.getIDList();
		WekaCluster cluster = new WekaCluster(matrix, idList);
		
		for(int i=startCNum; i<=endCNum; i++){
			System.out.println("Cluster Num: "+i);
			ClusterGroup testClusters = cluster.clusteringData(i);
			Evaluator.evaluateByPR(this.getTFClusters(dimeName), testClusters);
			Evaluator.evaluateByPurity(this.getTFClusters(dimeName), testClusters);
		}
	}
	
	//===WordNet + TfIdf clustering===
	public void useWnsTfidfClustering(String dimeName, int rankTh) throws Exception{
		System.out.println("use WordNet + TfIdf Clustering.....");
		
		int startCNum = 1;
		int endCNum = 10;
		TreeSet<String> patentIDs =(TreeSet<String>)this.getIDsFromClusters(dimeName);
		Set<String> featureSet = this.patentGroup.getFeatureSet(dimeName);
		PatentMapping pm = new PatentMapping(this.patentGroup, patentIDs);
		TermMapping tm = new TermMapping(featureSet);
		double[][] patentMatrix = pm.getPatentMatrix("tfidf", featureSet);
		double[][] termSimMatrix = tm.getSimMatrix();
		double[][] newPatentMatrix = JamaMath.getTimesMatrix(patentMatrix, termSimMatrix);
		ClusterValidity cv = this.prepareData(newPatentMatrix, pm.getIDList());
		
		for(int i=startCNum; i<=endCNum; i++){
			System.out.println("Cluster Num: "+i);
			ClusterGroup testClusters = this.constructMatrix(dimeName, i, rankTh);
			cv.setClusters(testClusters);
			cv.showValidity();
		}	
	}
	
	//===Effect+TfIdf clustering===
	public void useEffectTfidfClustering(int rankTh){
		System.out.println("use Effect + Tfidf Clustering.....");
		
		int startCNum = 1;
		int endCNum = 10;
		String dimeName = "effect";
		TreeSet<String> patentIDs =(TreeSet<String>)this.getIDsFromClusters(dimeName);
		Set<String> featureSet = this.patentGroup.getFeatureSet(dimeName);
		PatentMapping pm = new PatentMapping(this.patentGroup, patentIDs);
		double[][] patentEffectMatrix = pm.getPatentEffectMatrix("tfidf", featureSet);
		ClusterValidity cv = this.prepareData(patentEffectMatrix, pm.getIDList());
		
		for(int i=startCNum; i<=endCNum; i++){
			System.out.println("Cluster Num: "+i);
			ClusterGroup testClusters = this.constructMatrix(dimeName, i, rankTh);
			cv.setClusters(testClusters);
			cv.showValidity();
		}	
	}
	
	//===LSI+TfIdf clustering===
	public void useLSITfidfClustering(String dimeName, double kRatio, int rankTh){
		System.out.println("use LSI + Tfidf Clustering.....");
		
		int startCNum = 1;
		int endCNum = 10;
		TreeSet<String> patentIDs =(TreeSet<String>)this.getIDsFromClusters(dimeName);
		Set<String> featureSet = this.patentGroup.getFeatureSet(dimeName);
		PatentMapping pm = new PatentMapping(this.patentGroup, patentIDs);
		double[][] patentMatrix = pm.getPatentMatrix("tfidf", featureSet);
		LSI lsi = new LSI(patentMatrix, kRatio);
		double[][] lsiMatrix = lsi.getAk();
		ClusterValidity cv = this.prepareData(lsiMatrix, pm.getIDList());
		
		for(int i=startCNum; i<=endCNum; i++){
			System.out.println("Cluster Num: "+i);
			ClusterGroup testClusters = this.constructMatrix(dimeName, i, rankTh);
			cv.setClusters(testClusters);
			cv.showValidity();
		}	
		
	}
	
	//===LDA+TfIdf clustering===
	public void useLDATfidfClustering(String dimeName, int topicNum, int maxItr, int rankTh) throws Throwable{
		int startCNum = 1;
		int endCNum = 10;
		LDAProcessor lda = new LDAProcessor(this.patentGroup, (TreeSet)this.getIDsFromClusters(dimeName));
		lda.doLDA(dimeName, topicNum, maxItr);
		ClusterValidity cv = this.prepareData(lda.getDocTopicVecs());
		
		for(int i=startCNum; i<=endCNum; i++){
			System.out.println("Cluster Num: "+i);
			ClusterGroup testClusters = this.constructMatrix(dimeName, i, rankTh);
			cv.setClusters(testClusters);
			cv.showValidity();
		}	
	}

}
