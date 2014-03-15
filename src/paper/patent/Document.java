package paper.patent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.stanford.nlp.ling.HasWord;

public class Document {

	public static void main(String[] args) {
	}
	
	public static double getNormalize(double[] vector){
		double normalize = 0;
		
		for(double value : vector){
			normalize += (value * value);
		}
		normalize = Math.sqrt(normalize);
		return normalize;
	}
	
	protected String docID; //文件ID
	protected int size; //文件字數
	protected Map<String, Integer> termMap; //文字-字數對照表
	
	public Document(String docID){
		this.docID = docID;
		this.size = 0;
		this.termMap = new HashMap<String, Integer>();
	}
	
	public String getID(){
		return this.docID;
	}
	
	public Map<String, Integer> getTermMap(){
		return termMap;
	}
	
	public void showTerms(){
		Iterator<String> itr = this.termMap.keySet().iterator();
		while(itr.hasNext()){
			String term = itr.next();
			int freq = this.termMap.get(term);
			System.out.println(term+" "+freq);
		}
		System.out.println("total: "+this.termMap.size());
	}
	
	//===Add and Set Term====
	public void addTerm(String term, int freq){
		this.size += freq;
		
		if(this.termMap.containsKey(term)){
			int lastFreq = this.termMap.get(term);
			this.termMap.put(term, lastFreq + freq);
		}else{
			this.termMap.put(term, freq);
		}
	}
	
	public void addTermFromMap(Map<String, Integer> termMap){
		for(String term : termMap.keySet()){
			int freq = termMap.get(term);
			this.addTerm(term, freq);
		}
	}
	
	public void addTermFromCollection(Collection<String> terms){
		for(String term : terms){
			this.addTerm(term, 1);
		}
	}
	
	//====Get Term weight===
	public double getBinary(String term){
		double result = 0;		
		if(termMap.containsKey(term)){ 
			result = 1;	
		}
		return result;
	}
		
	public double getTF(String term){
		double result = 0;		
		if(termMap.containsKey(term)){
			result = termMap.get(term);
			//result = Math.log(result)/ Math.log(2);
		}
		return result;
	}
	
	public double getTfIdf(String term, InvertedIndex inverted){
		double tf_idf = 0;
		
		if(termMap.containsKey(term)){
			int term_tf = termMap.get(term);
			double term_idf = inverted.getIDF(term);
			tf_idf = term_tf*term_idf;
		}
		return tf_idf;
	}	
	
	//tfidf, tf, binary
	public double getTermWeight(String term, String vecType, InvertedIndex inverted){
		double weight = 0;
		
		if(vecType.equals("binary"))
			weight = this.getBinary(term);
		else if(vecType.equals("tf"))
			weight = this.getTF(term);
		else //tfidf
			weight = this.getTfIdf(term, inverted);	
		return weight;
	}
	
	public double[] getVector(String vecType, InvertedIndex inverted, Set<String> featureSet){
		double[] vector = new double[featureSet.size()];
		int index = 0;
		
		for(String term:featureSet){
			double value = this.getTermWeight(term, vecType, inverted);
			vector[index] = value;
			index++;
		}
		return vector;
	}
}
