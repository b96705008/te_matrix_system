package paper.techmatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;

import paper.Util;
import paper.backend.Database;
import paper.clustering.cluster.ClusterGroup;
import paper.clustering.cluster.ClusterValidity;
import paper.clustering.mahout.LDAProcessor;
import paper.clustering.mahout.MahoutCluster;
import paper.clustering.mahout.SequenceIO;
import paper.patent.PatentGroup;
import paper.semantic.PatentMapping;
import paper.semantic.TermMapping;
import paper.semantic.matrix.JamaMath;
import paper.semantic.matrix.LSI;
import paper.textprocess.Feature;

public class MatrixSystem {

	public static void main(String[] args) throws Throwable {
		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
		//System.out.print("請輸入一列文字: "); 
		//String text = buf.readLine(); 
		//System.out.println("您輸入的文字: " + text); 
		
		//===parameter setting===
		String field = "DDS"; //DDS, LEDS, QDGP, SRD, CN
		
		//tech related params
		String techVecType = "tfidf"; //tfidf, wordnet, lsi, lda
		double tech_lsi_k = 0.6;
		int tech_lda_topicNum = 20;			
		boolean techFindBestK = true; //false->given k, true->use cv to find suitable k
		int techK = 3; //cluster number
		String techCvType = "MinMaxCNP"; //MinMax, MinMaxCNP, DBIndex
		
		//effect related params
		String effectVecType = "effect"; //tfidf, wordnet, lsi, lda, effect
		double effect_lsi_k = 0.6;
		int effect_lda_topicNum = 20;
		boolean effectFindBestK = false; //false->single, true->iterative clustering
		int effectK = 3; //cluster number
		String effectCvType = "MinMaxCNP"; //MinMax, MinMaxCNP, DBIndex
		
		//read parameters
		System.out.print("請輸入科技領域 (DDS, LEDS, QDGP, SRD, CN): ");
		field = buf.readLine().toUpperCase().trim();
		
		System.out.println("===技術面的選項===");
		System.out.print("技術部分所使用的vector transformation方法 (tfidf, wordnet, lsi, lda)? ");
		techVecType = buf.readLine().toLowerCase().trim();
		if(techVecType.equals("lsi")){
			System.out.print("LSI的k希望是多少%的feature number? ");
			tech_lsi_k = Double.parseDouble(buf.readLine().toLowerCase().trim());
		}else if (techVecType.equals("lda")){
			System.out.print("LDA的topic number? ");
			tech_lda_topicNum = Integer.parseInt(buf.readLine().toLowerCase().trim());
		}
		System.out.print("技術方面是否用 Cluster validity 幫你決定最佳群數 (yes or no)? ");
		techFindBestK = buf.readLine().toLowerCase().trim().equals("yes") ? true : false;
		if(techFindBestK){
			System.out.print("請選擇Cluster validity的方法 (MinMax, MinMaxCNP, DBIndex): ");
			techCvType = buf.readLine().trim();
		}else{
			System.out.print("請選擇群數: ");
			techK = Integer.parseInt(buf.readLine().trim());
		}
			
		System.out.println("===功效面的選項===");
		System.out.print("功效部分所使用的vector transformation方法 (tfidf, wordnet, lsi, lda, effect)? ");
		effectVecType = buf.readLine().toLowerCase().trim();
		if(effectVecType.equals("lsi")){
			System.out.print("LSI的k希望是多少%的feature number? ");
			effect_lsi_k = Double.parseDouble(buf.readLine().toLowerCase().trim());
		}else if (effectVecType.equals("lda")){
			System.out.print("LDA的topic number? ");
			effect_lda_topicNum = Integer.parseInt(buf.readLine().toLowerCase().trim());
		}
		System.out.print("功效方面是否用 Cluster validity 幫你決定最佳群數 (yes or no)? ");
		effectFindBestK = buf.readLine().toLowerCase().trim().equals("yes") ? true : false;
		if(effectFindBestK){
			System.out.print("請選擇Cluster validity的方法 (MinMax, MinMaxCNP, DBIndex): ");
			effectCvType = buf.readLine().trim();
		}else{
			System.out.print("請選擇群數: ");
			effectK = Integer.parseInt(buf.readLine().trim());
		}
		
		//===select field===
		MatrixSystem system = new MatrixSystem(field);
																	
		//===tech part===
		ClusterGroup techClusters = 
			system.generateClusters("tech", techVecType, techFindBestK, 
					techK, techCvType, tech_lsi_k, tech_lda_topicNum);
		
		//===effect part===
		ClusterGroup effectClusters = 
			system.generateClusters("effect", effectVecType, effectFindBestK, 
					effectK, effectCvType, effect_lsi_k, effect_lda_topicNum);
		
		//===generate matrix===
		MatrixSystem.generateMatrix(techClusters, effectClusters);
	}
	
	//===construct matrix===
	public static void generateMatrix(ClusterGroup techClusters, ClusterGroup effectClusters){
		Set<String> techClusterIDs = techClusters.getClusterIDs();
		Set<String> effectClusterIDs = effectClusters.getClusterIDs();
		System.out.println("\nResult:");
		System.out.println("(techID, effectID): [patentIDs]");
		for(String techClusterID : techClusterIDs) {
			Set<String> techCluster = techClusters.getClusterByID(techClusterID);
			
			for(String effectClusterID: effectClusterIDs) {
				Set<String> effectCluster = effectClusters.getClusterByID(effectClusterID);
				Set<String> intersetIDs = Util.getIntersectSet(techCluster, effectCluster);
				System.out.println("("+techClusterID + ","+ effectClusterID + "): " 
						+ intersetIDs);
			}
		}
		
	}
	
	private String field;
	private Map<String, ClusterGroup> clustersMap;
	private PatentGroup patentGroup; //patents for test 
	
	private MatrixSystem(String field, int level){
		this.field = field;
		this.initClustersMap();
		Database db =  Database.getInstance();
		Set<String> patentIDs = db.processTFData(field, this.clustersMap, level);
		this.patentGroup = new PatentGroup(patentIDs);
	}
	
	public MatrixSystem(String field){
		this(field, 1);
	}
	
	//====property process====
	private void initClustersMap(){
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
	
	//===mahout data process===
	
	//prepare data for mahout clustering 
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
	
	//construct by clustering
	public ClusterGroup clusteringData(String dimeName, int k) {
		ClusterGroup testClusters = null;
		int rankTh = 2;
		
		try {
			testClusters = MahoutCluster.clusterData(k, rankTh, 1.0/k);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return testClusters;
	}
	
	//
	
	//=== Vector transformation method===
	
	//TF-IDF
	public ClusterValidity useTfIdfVector(String dimeName) throws Exception{
		System.out.println("use TfIdf Vector.....");
		ClusterValidity cv = this.prepareData("tfidf", dimeName);
		
		return cv;
	}
	
	//WordNet + TF-IDF
	public ClusterValidity useWordNetBasedVector(String dimeName) throws Exception{
		System.out.println("use WordNet + TfIdf vector.....");
		
		TreeSet<String> patentIDs = (TreeSet<String>)this.getIDsFromClusters(dimeName);
		Set<String> featureSet = this.patentGroup.getFeatureSet(dimeName);
		PatentMapping pm = new PatentMapping(this.patentGroup, patentIDs);
		TermMapping tm = new TermMapping(featureSet);
		double[][] patentMatrix = pm.getPatentMatrix("tfidf", featureSet);
		double[][] termSimMatrix = tm.getSimMatrix();
		double[][] newPatentMatrix = JamaMath.getTimesMatrix(patentMatrix, termSimMatrix);
		ClusterValidity cv = this.prepareData(newPatentMatrix, pm.getIDList());
		
		return cv;
	}
	
	//LSI + TF-IDF
	public ClusterValidity useLSIBasedVector(String dimeName, double kRatio) throws Exception{
		System.out.println("use LSI + Tfidf Clustering.....");
		
		TreeSet<String> patentIDs = (TreeSet<String>)this.getIDsFromClusters(dimeName);
		Set<String> featureSet = this.patentGroup.getFeatureSet(dimeName);
		PatentMapping pm = new PatentMapping(this.patentGroup, patentIDs);
		double[][] patentMatrix = pm.getPatentMatrix("tfidf", featureSet);
		LSI lsi = new LSI(patentMatrix, kRatio);
		double[][] lsiMatrix = lsi.getAk();
		ClusterValidity cv = this.prepareData(lsiMatrix, pm.getIDList());
		
		return cv;
	}
	
	//LDA + TF
	public ClusterValidity useLDABasedVector(String dimeName, int topicNum) throws Throwable{
		System.out.println("use LDA + TF Clustering.....");
		
		int maxItr = 100;
		LDAProcessor lda = 
			new LDAProcessor(this.patentGroup, (TreeSet)this.getIDsFromClusters(dimeName));
		lda.doLDA(dimeName, topicNum, maxItr);
		ClusterValidity cv = this.prepareData(lda.getDocTopicVecs());
		
		return cv;	
	}
	
	//Effect Vector (Effect score * TF-IDF)
	public ClusterValidity useEffectVector(){
		System.out.println("use Effect + Tfidf Clustering.....");
		
		String dimeName = "effect";
		TreeSet<String> patentIDs =(TreeSet<String>)this.getIDsFromClusters(dimeName);
		Set<String> featureSet = this.patentGroup.getFeatureSet(dimeName);
		PatentMapping pm = new PatentMapping(this.patentGroup, patentIDs);
		double[][] patentEffectMatrix = pm.getPatentEffectMatrix("tfidf", featureSet);
		ClusterValidity cv = this.prepareData(patentEffectMatrix, pm.getIDList());
		
		return cv;
	}
	
	//=== Fuzzy C-Means clustering===
	
	//clustering given cluster number - k
	public ClusterGroup clusteringByK(String dimeName, int k, ClusterValidity cv){
		System.out.println("Cluster Num: " + k);
		ClusterGroup clusters = this.clusteringData(dimeName, k);
		cv.setClusters(clusters);
		cv.showValidity();
		
		return clusters;
	}
	
	//不給定K, 用ClusterValidity的值選最佳K
	public ClusterGroup clusteringByCV(String dimeName, ClusterValidity cv, String cvType){
		int startCNum = 2;
		int endCNum = 10;
		double cvMinValue = Double.MAX_VALUE;
		ClusterGroup selectedClusters = null;
		
		for(int k = startCNum; k <= endCNum; k++){
			System.out.println("current cluster num: " + k);
			ClusterGroup currentClusters = this.clusteringData(dimeName, k);
			
			//cv process
			cv.setClusters(currentClusters);
			double currentCvValue = cv.getCVByType(cvType);
			System.out.println(cvType + ": " + currentCvValue);
			
			if(currentCvValue < cvMinValue) {
				selectedClusters = currentClusters;
				cvMinValue = currentCvValue;
			}
		}
		
		System.out.println("final selected cluster num: " + selectedClusters.getClustersSize());
		
		return selectedClusters;
	}
	
	//===cluster labeling===
	public void labelClusterByDime(String dimeName, ClusterGroup testClusters){
		if(dimeName.equals("tech"))
			Feature.labelCluster(this.patentGroup.getInvertedIndex(), 
				this.patentGroup.getFeatureSet(dimeName), testClusters, 15, true);
		else //effect
			Feature.labelCluster(this.patentGroup.getInvertedIndex(), 
					this.patentGroup.getFilteredEffectTerms(), testClusters, 15, false);
	}
	
	//===Final===
	//generateClusters
		public ClusterGroup generateClusters(String dimeName, String vecType, 
				boolean findBestK, int k, String cvType, double lsi_k, int lda_topicNum) throws Throwable{
			System.out.println("\n"+dimeName+" generateClustersByParams:");
			ClusterValidity cv = null;
			
			if(vecType.equals("tfidf")) 
				cv = this.useTfIdfVector(dimeName);
			else if(vecType.equals("wordnet"))
				cv = this.useWordNetBasedVector(dimeName);
			else if(vecType.equals("lsi"))
				cv = this.useLSIBasedVector(dimeName, lsi_k); 
			else if(vecType.equals("lda"))
				cv = this.useLDABasedVector(dimeName, lda_topicNum);
			else {
				if(dimeName.equals("effect"))
					cv = this.useEffectVector();
				else
					cv = this.useTfIdfVector("tech");
			}
			
			ClusterGroup clusters = null;
			if(findBestK){
				clusters = this.clusteringByCV(dimeName, cv, cvType);
			}else{
				clusters = this.clusteringByK(dimeName, k, cv);
			}
			
			System.out.println("\n"+dimeName+" cluster labeling:");
			this.labelClusterByDime(dimeName, clusters);
			
			return clusters;
		}
	
}
