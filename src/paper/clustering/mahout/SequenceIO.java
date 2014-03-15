package paper.clustering.mahout;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.VectorWritable;

import paper.Config;
import paper.clustering.cluster.ClusterGroup;
import paper.clustering.cluster.ClusterProcessor;
import paper.patent.Document;
import paper.patent.InvertedIndex;
import paper.patent.Patent;
import paper.patent.PatentGroup;

public class SequenceIO {
	
	 public static void main(String args[]) throws Exception {
		 showFile(Config.vectorFile);
	 }
	
	public static Configuration CONF = new Configuration();
	public static FileSystem FS;
	static{
	    try {
	    	FS = FileSystem.get(CONF);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//writer
	public static void generateVectorFile(String vecType, String dimeName, 
			 PatentGroup patentGroup) throws IOException{
		System.out.println("generateVectorFile....");
		
		//hadoop initialize 
	    SequenceFile.Writer writer = 
	    	new SequenceFile.Writer(FS, CONF, new Path(Config.vectorFile), Text.class, VectorWritable.class);
	    
	    //ready to write
		Set<String> feats = patentGroup.getFeatureSet(dimeName);
		InvertedIndex inverted = patentGroup.getInvertedIndex();
		
		for(Patent patent : patentGroup){
			double[] vector = patent.getVector(vecType, inverted, feats);
			NamedVector nameVector = new NamedVector(
					new RandomAccessSparseVector(new DenseVector(vector)), patent.getID());
            writer.append(new Text(nameVector.getName()), new VectorWritable(nameVector));
		}
		
	    writer.close();
	}
	
	public static void generateVectorFile(String vecType, String dimeName, 
			 PatentGroup patentGroup, Collection<String> selectedPatentIDs) throws IOException{
		System.out.println("generateVectorFile....");
		
	    SequenceFile.Writer writer = 
	    	new SequenceFile.Writer(FS, CONF, new Path(Config.vectorFile), Text.class, VectorWritable.class);
	    
	    //ready to write
  		Set<String> feats = patentGroup.getFeatureSet(dimeName);
  		InvertedIndex inverted = patentGroup.getInvertedIndex();
  		
  		for(String patentID : selectedPatentIDs){
  			Patent patent = patentGroup.getPatentByID(patentID);
  			double[] vector = patent.getVector(vecType, inverted, feats);
  			NamedVector nameVector = new NamedVector(
  					new RandomAccessSparseVector(new DenseVector(vector)), patent.getID());
            writer.append(new Text(nameVector.getName()), new VectorWritable(nameVector));
  		}
  		
  	    writer.close();
	}
	
	public static void generateVectorFile(double[][] matrix, List<String> idList) throws IOException{
		System.out.println("generateVectorFile....");
		
	    SequenceFile.Writer writer = 
	    	new SequenceFile.Writer(FS, CONF, new Path(Config.vectorFile), Text.class, VectorWritable.class);
	    
	    for(int i=0; i<matrix.length; i++){
	    	String id = idList.get(i);
	    	double[] vector = matrix[i];
	    	NamedVector nameVector = new NamedVector(
  					new RandomAccessSparseVector(new DenseVector(vector)), id);
	    	writer.append(new Text(nameVector.getName()), new VectorWritable(nameVector));
	    }
	    writer.close();
	}
	
	public static void generateVectorFile(Map<String, DenseVector> denVecMap) throws IOException{
		System.out.println("generateVectorFile....");
		  SequenceFile.Writer writer = 
			    	new SequenceFile.Writer(FS, CONF, new Path(Config.vectorFile), Text.class, VectorWritable.class);
		for(String id : denVecMap.keySet()){
			DenseVector denVec = denVecMap.get(id);
			NamedVector nameVector = 
					new NamedVector(new RandomAccessSparseVector(denVec), id);
			writer.append(new Text(nameVector.getName()), new VectorWritable(nameVector));
		}
		writer.close();
	}
	
	public static void generateDictionary(String dimeName, PatentGroup patentGroup) throws IOException{
		//hadoop initialize 
		SequenceFile.Writer writer = 
				new SequenceFile.Writer(FS, CONF, new Path(Config.dictFile), Text.class, IntWritable.class);
		
		//ready to write
		Set<String> featureSet = patentGroup.getFeatureSet(dimeName);
		int index = 0;
		
		for(String feature : featureSet){
			writer.append(new Text(feature), new IntWritable(index));
			index++;
		}
		writer.close();
	}
	
	//read
    public static Map<String, NamedVector> readVector() throws IOException{
    	//declare
    	Map<String, NamedVector> vectorMap = new TreeMap<String, NamedVector>();
        SequenceFile.Reader reader = new SequenceFile.Reader(FS, new Path(Config.vectorFile), CONF);
        
        Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), CONF);
        VectorWritable vecValue = new VectorWritable();
       
        while (reader.next(key, vecValue)) {
        	NamedVector termVec = (NamedVector) vecValue.get();
        	vectorMap.put(termVec.getName(), termVec);
        }
        reader.close();
        
        return vectorMap;
    }
	
	//read
    public static ClusterGroup readClusters(String inputPath, int rankThold, 
    		double weightThold) throws IOException{
	   	Path path = new Path(inputPath);
        SequenceFile.Reader reader = new SequenceFile.Reader(FS, path, CONF);
        
        ClusterGroup clusters = new ClusterGroup();
    	ClusterProcessor cProcessor = new ClusterProcessor();
        Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), CONF);
        WeightedVectorWritable wVecValue = new WeightedVectorWritable();
        while (reader.next(key, wVecValue)) {
        	String clusterID = key.toString();
        	double weight = wVecValue.getWeight();
        	String patentID = ((NamedVector) wVecValue.getVector()).getName();
        	cProcessor.addClusterElem(patentID, weight, clusterID);
        }
        clusters = cProcessor.getClusters(rankThold, weightThold);
      
        reader.close();
        return clusters;
    }
    
    public static void showFile(String inputPath) throws IOException{
    	System.out.println("Readeing Sequence File...");
	   	Path path = new Path(inputPath);
        SequenceFile.Reader reader = new SequenceFile.Reader(FS, path, CONF);
        
        //ready to read
        Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), CONF);
        Writable value = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), CONF);
        while (reader.next(key, value)) {        
        	System.out.println(key.toString()+" "+value.toString());
        }	
        
       reader.close();
    }
    
    //===LDA===
    //read docOut
    public static Map<String, DenseVector> readLdaDocOut(String docOutPath, 
    		String docIndexPath) throws IOException{
    	//declare
    	Map<String, DenseVector> docToTopics = new TreeMap<String, DenseVector>();
        SequenceFile.Reader docOutReader = new SequenceFile.Reader(FS, new Path(docOutPath), CONF);
        SequenceFile.Reader docIndexReader = new SequenceFile.Reader(FS, new Path(docIndexPath), CONF);
        
        //ready to read
        //docOut
        Writable docOutKey = (Writable) ReflectionUtils.newInstance(docOutReader.getKeyClass(), CONF);
        VectorWritable vecValue = new VectorWritable();
        //docIndex
        Writable docIndexkey = (Writable) ReflectionUtils.newInstance(docIndexReader.getKeyClass(), CONF);
        Writable docIndexValue = (Writable) ReflectionUtils.newInstance(docIndexReader.getValueClass(), CONF); 
        
        while (docOutReader.next(docOutKey, vecValue)) {
        	docIndexReader.next(docIndexkey, docIndexValue);
        	String patentID = docIndexValue.toString();
        	DenseVector topicVec = (DenseVector)vecValue.get();
        	docToTopics.put(patentID, topicVec);	
        }            
        docOutReader.close();
        docIndexReader.close();
        
    	return docToTopics;
    }
    
    //read topicOut
    public static Map<String, DenseVector> readLdaTopicOut(String topicOutPath) throws IOException{
    	//declare
    	Map<String, DenseVector> topicToTerms = new TreeMap<String, DenseVector>();
    	SequenceFile.Reader topicOutReader = new SequenceFile.Reader(FS, new Path(topicOutPath), CONF);
    	
    	//ready to read
    	Writable topicOutKey = (Writable) ReflectionUtils.newInstance(topicOutReader.getKeyClass(), CONF);
        VectorWritable vecValue = new VectorWritable();
        
        while(topicOutReader.next(topicOutKey, vecValue)){
        	String clusterID = topicOutKey.toString();
        	DenseVector termVec = (DenseVector)vecValue.get();
        	topicToTerms.put(clusterID, termVec);
        }
        topicOutReader.close();
        
    	return topicToTerms;
    }
}
