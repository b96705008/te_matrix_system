package paper;

public class Config {
	//Clustering
	public static String clusteringDir = "data/clustering/";
	public static String clusterCentroids = clusteringDir + "cluster-centroids/";
	public static String clusterOutput = clusteringDir + "cluster-output/";
	public static String clusterPointsFile =  clusterOutput+"/clusteredPoints/part-m-0";
	//===data for clustering===
	public static String clusterDataDir =  clusteringDir + "cluster-data/";
	public static String vectorDir = clusterDataDir + "data-vectors/";
	public static String vectorFile = vectorDir + "vector";
	//===for Matrix====
	public static String matrixDir = clusterDataDir + "data-matrix/";
	public static String matrixFile = matrixDir + "matrix";
	public static String docIndexFile = matrixDir + "docIndex";
	//===for LDA====
	public static String dictFile = clusterDataDir + "dictionary";
	public static String ldaDir = clusterDataDir + "lda/";
	public static String docOutFile = ldaDir + "doc_Out";
	public static String topicOutFile = ldaDir + "topic_Out";
	
	//Dimension Reducion
	public static String svdFolder = clusteringDir + "svd/";
	public static String eigenFolder = svdFolder + "data-eigen/";
	public static String matrixFolder = svdFolder + "data-matrix/";
	
	//NLP
	public static String nlpDir = "data/nlp/";
	public static String taggerModelFile = nlpDir + "tagger/wsj-0-18-left3words.tagger";
	public static String parserModelFile = nlpDir + "parser/englishPCFG.ser.gz";
	
	//Keyword
	public static String keywordDir = "data/keyword/";
	public static String stopListFile = keywordDir + "stop_list.txt";
	public static String patentStopListFile = keywordDir + "patent_stop_list.txt";
	public static String techDic = keywordDir + "dic_tech.txt";
	public static String FuncDic = keywordDir + "dic_func.txt";
	public static String Dic = keywordDir + "dictionary.txt";
	
	//excel
	public static String dwpiFile = "data/patent_dwpi.xls";
	
	//function
	public static String funcWordsFile = "data/function_word_0.95.txt";
	
	//WordNet
	public static String wordNetDir = "/Users/roger19890107/Desktop/programming_tool/WordNet/";
	
	//patent phrasesObj IO
	public static String phrasesDir = "data/phrases/";

}
